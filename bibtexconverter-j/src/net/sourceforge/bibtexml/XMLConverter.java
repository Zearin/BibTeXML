package net.sourceforge.bibtexml;
/*
 * $Id: BibTeXConverter.java 146 2007-03-21 12:01:46Z ringler $
 * (c) Moritz Ringler, 2006

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import de.mospace.lang.DefaultClassLoaderProvider;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLConverter extends DefaultClassLoaderProvider{
    private TransformerFactory tf;
    private Validator xmlValidator;
    protected final static Charset DEFAULT_ENC = Charset.defaultCharset();
    protected final static String INTERNAL_PARAMETER_PREFIX = "bibtexml.sf.net.";
    public final static String RELAXNG_SF
        = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;
    public final static String JARV_RELAXNG_SF
        = "org.iso_relax.verifier.jaxp.validation.RELAXNGSchemaFactoryImpl";
    private Charset xmlenc = DEFAULT_ENC;
    public final static String TRANSFORMER_FACTORY_IMPLEMENTATION =
            "net.sf.saxon.TransformerFactoryImpl";
    protected ErrorHandler saxErrorHandler = new ErrorCounter();
    private String xmlSchemaID = null;

    public XMLConverter(){
        /* add default library directories to class path if they exist */
        String fs = File.separator;
        List<String> candidates = new ArrayList<String>();
        candidates.add(System.getProperty("user.dir") + fs + "lib");
        try{
            candidates.add(new File(
                DefaultClassLoaderProvider
                .getRepositoryRootDir(BibTeXConverter.class),
                "lib").getAbsolutePath());
        } catch(Exception ignore){
            System.err.println(ignore);
            System.err.flush();
        }
        String appdata = System.getenv("APPDATA");
        if(appdata != null){
            candidates.add(appdata + fs + "bibtexconverter");
        }
        candidates.add(
            System.getProperty("user.home")+fs+".bibtexconverter"+fs+"lib");
        candidates.add(
            Preferences.userNodeForPackage(BibTeXConverter.class)
                    .get("saxon", null));
        for (String cf : candidates){
            if(cf != null){
                File f = new File(cf);
                if(f.isDirectory() && !DefaultClassLoaderProvider.isTemporary(f)){
                    registerLibraryDirectory(f);
                } else if(f.isFile() && f.getName().endsWith(".jar")){
                    registerLibrary(f);
                }
            }
        }


        /* Try to obtain a Saxon transformer factory */
        System.setProperty("javax.xml.transform.TransformerFactory",
                            TRANSFORMER_FACTORY_IMPLEMENTATION);
        tf = tryToGetTransformerFactory();

        /* Restore preferred validation engine */
        for (String schemaLanguage :
            new String[]{
                XMLConstants.RELAXNG_NS_URI,
                XMLConstants.W3C_XML_SCHEMA_NS_URI
            }){
            String key = "javax.xml.validation.SchemaFactory:" + schemaLanguage;
            String prefVal = Preferences.userNodeForPackage(getClass()).node("schema").get(key, null);
            if(prefVal != null){
                System.setProperty(key, prefVal);
            }
        }
    }

    public void setXMLEncoding(Charset chars){
        xmlenc = chars;
    }

    public Charset getXMLEncoding(){
        return xmlenc;
    }

    public void setValidationErrorHandler(ErrorHandler handler){
        saxErrorHandler = handler;
    }

    /** @throws IllegalArgumentException if schema has an unknown extension or
    * no SchemaFactory for its language is available. */
    public synchronized void setXMLSchema(URL schema) throws SAXException{
        if(schema == null){
            xmlValidator = null;
            return;
        }
        String schemaLanguage = null;
        String schemaName = schema.toString();
        if(schemaName.endsWith(".xsd")){
            schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        } else if(schemaName.endsWith(".rng")){
            schemaLanguage = XMLConstants.RELAXNG_NS_URI;
        } else {
            throw new IllegalArgumentException("URL must end with .xsd or .rng");
        }
        xmlValidator = getSchemaFactory(schemaLanguage).newSchema(schema).newValidator();
        xmlSchemaID = schema.toString();
    }

    public synchronized void setXMLSchema(Source schema, String schemaLanguage) throws SAXException{
        xmlValidator = getSchemaFactory(schemaLanguage).newSchema(schema).newValidator();
        xmlSchemaID = schema.getSystemId();
    }

    public String getXMLSchemaID(){
        return xmlSchemaID;
    }

    private SchemaFactory getSchemaFactory(String schemaLanguage){
        SchemaFactory sf = null;
        try{
            sf = SchemaFactory.newInstance(schemaLanguage);
        } catch(IllegalArgumentException ex){
            if(schemaLanguage.equals(XMLConstants.RELAXNG_NS_URI)){
                try{
                    sf = (SchemaFactory) Class.forName("org.iso_relax.verifier.jaxp.validation.RELAXNGSchemaFactoryImpl").newInstance();
                } catch (Exception ex2){
                    ex2.printStackTrace();
                    throw new IllegalArgumentException("No service provider found for schema language " + schemaLanguage);
                }
            } else {
                throw new IllegalArgumentException("No service provider found for schema language " + schemaLanguage);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return sf;
    }

    public boolean hasSchema(){
        return xmlValidator != null;
    }

    /** @return the number of errors that occured */
    public synchronized void validate(File xml) throws SAXException, IOException{
        if(xmlValidator != null){
            InputStream in = null;
            try{
                in = new BufferedInputStream(new FileInputStream(xml));
                Source src = new SAXSource(new InputSource(in));
                src.setSystemId(xml.toURI().toURL().toString());
                xmlValidator.setErrorHandler(saxErrorHandler);
                xmlValidator.validate(src);
            } finally {
                if (in != null){
                    in.close();
                }
            }
        }
    }

    public String getSaxonVersion(){
        return getSaxonVersion("ProductTitle");
    }

    public String getSaxonVersion(String what){
        String result = null;
        try{
            Class c = Class.forName("net.sf.saxon.Version", true, getClassLoader());
            java.lang.reflect.Method m = c.getMethod("get" + what);
            result = (String) m.invoke(null);
        } catch (Exception ignore){
            System.err.println(ignore);
            System.err.flush();
        }
        return result;
    }

    /* XSLT transformations */
    /** Converts BibXML using the specified transformer and configuration.
     * The result is optionally converted
     *  to CRLF format. **/
    public static void transform(Transformer t, InputStream in, String systemID, OutputStream out,
            Map<String,Object> parameters, String encoding, boolean crlf)
            throws TransformerException, IOException{

        // configure the Transformer
        t.clearParameters();
        if (parameters != null){
            Set<String> keys = parameters.keySet();
            for(String key: keys){
                t.setParameter(key, parameters.get(key));
            }
        }
        if(encoding != null){
            t.setParameter(INTERNAL_PARAMETER_PREFIX +"encoding", encoding);
            t.setOutputProperty(OutputKeys.ENCODING, encoding);
        }


        //open the source xml document
        InputStream bin = new BufferedInputStream(in);
        Source src = new StreamSource(bin);
        src.setSystemId(systemID);
        //open the target xml document
        OutputStream bout = new BufferedOutputStream(out);
        if(crlf){
            bout = new CRLFOutputStream(bout);
        }
        Result res = new StreamResult(bout);

        t.transform(src, res);
    }

    /** Converts BibXML using the specified transformer and configuration.
     * The result is optionally converted
     *  to CRLF format. **/
    public static void transform(Transformer t, File input, File output,
            Map<String,Object> parameters, String encoding, boolean crlf)
            throws TransformerException, IOException{

        //open the source xml document
        InputStream in = new FileInputStream(input);
        OutputStream out = null;

        try{
            out = new FileOutputStream(output);
            transform(t, in, input.toURI().toURL().toString(), out, parameters, encoding, crlf);
        } finally {
            if(in != null){
                try{
                    in.close();
                } catch (IOException ignore) {
                    System.err.println(ignore);
                    System.err.flush();
                }
            }
            if(out != null){
                try{
                    out.close();
                } catch (IOException ignore) {
                    System.err.println(ignore);
                    System.err.flush();
                }
            }
        }
    }

    protected Transformer loadStyleSheet(ClassLoader cl, String resource) throws TransformerConfigurationException{
        InputStream in = (cl == null)
            ? getClass().getResourceAsStream(resource)
            : cl.getResourceAsStream(resource);
        URL sysID = (cl == null)
            ? getClass().getResource(resource)
            : cl.getResource(resource);
        Transformer result = null;
        if(in == null){
            handleException("Stylesheet "+resource+" not found.", null);
        } else {
            result = loadStyleSheet(in, sysID);
            try{
                in.close();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    protected Transformer loadStyleSheet(InputStream in, URL systemId) throws TransformerConfigurationException{
        Transformer t = null;
        Source styleSrc = new StreamSource(in);
        if(systemId != null){
            styleSrc.setSystemId(systemId.toString());
        }
        t = tf.newTransformer(styleSrc);
        return t;
    }

    /* Helper methods */

    /** Tries to obtain an instance of a Saxon TransformerFactory.
    * If Saxon is on the application class path or in one of BibTeXConverter's
    * installation-dependent library directories a Saxon transformer factory
    * will be created and returned by calls to this method.<p>
    * If Saxon has not been found in one of the above locations. This method
    * asks the user to install Saxon. If the installation completes
    * successfully, a Saxon transformer factory is created and returned
    * by this and all future calls to this method.
    * @return a Saxon TransformerFactory or null as detailed above
    */
    protected synchronized TransformerFactory loadTransformerFactory(final JFrame trig){
        if(tf == null){
            /* We first try to load from the existing class path and library
             * directories.
             */
            tf = tryToGetTransformerFactory();
        }

        if(tf == null){
            /* We've been unsuccessfull so far. We ask the user to install
            * saxon.
            */
            if(About.installSaxon(trig, this)){
                /* if the user has indeed installed saxon
                * we update our custom class loader and
                * try to create a TransformerFactory */
                tf = tryToGetTransformerFactory();
            }
        }
        return tf;
    }

    /** Tries to obtain an instance of a Saxon TransformerFactory. If saxon
     * is not found, null is returned.
     **/
    public synchronized TransformerFactory tryToGetTransformerFactory(){
        if(tf == null){
            Thread.currentThread().setContextClassLoader(getClassLoader());
            try{
                tf = TransformerFactory.newInstance();
                System.out.println("Saxon found in " +
                        DefaultClassLoaderProvider.getRepositoryRoot(
                        tf.getClass()));
                double saxonversion = 0;
                try{
                    String sv = getSaxonVersion("ProductVersion");
                    //use only part up to second dot
                    int dot = sv.indexOf('.');
                    dot = sv.indexOf('.', dot + 1);
                    if(dot > 0){
                        sv = sv.substring(0, dot);
                    }
                    saxonversion = Double.parseDouble(sv);
                } catch (Exception ignore){
                    System.err.println("Cannot parse saxon version.");
                    System.err.println(ignore);
                    System.err.flush();
                }
                System.out.println("Saxon version: " + saxonversion);
                if(saxonversion >= 8.9){
                    System.out.println();
                } else if (saxonversion >= 8.8){
                    //We need to switch the URI resolver to something that
                    //knows how to handle jar files
                    tf.setURIResolver(new JarAwareURIResolver());
                } else {
                    System.out.println();
                    System.err.println("WARNING: This program has been developed" +
                        " and tested with saxon version 8.8 and later.");
                }
                System.out.flush();
                System.err.flush();
            } catch (TransformerFactoryConfigurationError ignore){
            }
        }
        return tf;
    }

    /** Copies a resource to the specified destination file. */
    protected void copyResourceToFile(String resourcename, File dest)
         throws IOException{
        File target = dest;
        if(!target.isDirectory()){
            target = target.getAbsoluteFile().getParentFile();
        }
        int lastslash = resourcename.lastIndexOf('/');
        if(lastslash == resourcename.length()){
            throw new IllegalArgumentException("Resourcename may not end with a slash.");
        }
        target = new File(target, (lastslash < 0) ? resourcename
                : resourcename.substring(lastslash + 1));
        InputStream in = null;
        OutputStream out = null;
        try{
            int count;
            int BUFFER = 2048;
            byte data[] = new byte[BUFFER];

            in = this.getClass().getResourceAsStream(resourcename);
            in = new BufferedInputStream(in);
            out = new FileOutputStream(target);
            out = new BufferedOutputStream(out);

            while ((count = in.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }

        } finally {
            if(in != null){
                try{
                    in.close();
                } catch (IOException ignore){
                    System.err.println(ignore);
                    System.err.flush();
                }
            }
            if(out != null){
                try{
                    out.close();
                } catch (IOException ignore){
                    System.err.println(ignore);
                    System.err.flush();
                }
            }
        }
    }

    /* this dhould go into the controller in the medium term */
    protected void handleException(String message, Throwable ex){
        if(message != null){
            System.err.println(message);
            System.err.flush();
        }
        if(ex != null){
            System.err.println(ex);
            System.err.flush();
        }
    }

    private static class JarAwareURIResolver implements URIResolver{
        public JarAwareURIResolver(){
        }

        public Source resolve(String href,
                      String base)
                throws TransformerException{
           try{
               URI uri = new URI(href);
               if(uri.isAbsolute()){
                   //leave it as it is
               } else if (base.startsWith("jar:")){
                   int lastslash = base.lastIndexOf('/');
                   if(lastslash >= 0){
                       uri = new URI(base.substring(0, lastslash + 1) + href);
                   } else {
                       uri = new URI(base + "/" + href);
                   }
               } else {
                   URI baseuri = new URI(base);
                   uri = baseuri.resolve(uri);
               }
               URL url = uri.toURL();
               InputStream in = url.openStream();
               Source src = new StreamSource(new BufferedInputStream(in));
               src.setSystemId(url.toString());
               return src;
           } catch (Exception ex){
               throw new TransformerException(ex);
           }
        }
    }
}