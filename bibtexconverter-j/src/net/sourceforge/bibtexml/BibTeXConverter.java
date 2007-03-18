package net.sourceforge.bibtexml;
/*
 * $Id: BibTeXConverter.java,v 1.21 2007/03/10 22:59:56 Moritz.Ringler Exp $
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.*;
import javax.xml.XMLConstants;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import de.mospace.lang.DefaultClassLoaderProvider;
import de.mospace.swing.LookAndFeelMenu;

public class BibTeXConverter extends DefaultClassLoaderProvider{
    private final static boolean cleanInput = true;
    private TransformerFactory tf;
    private Validator xmlValidator; 
    protected final static String DEFAULT_ENC = Charset.defaultCharset().name();
    protected final static String INTERNAL_PARAMETER_PREFIX = "bibtexml.sf.net.";
    public final static String RELAXNG_SF 
        = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;
    public final static String JARV_RELAXNG_SF
        = "org.iso_relax.verifier.jaxp.validation.RELAXNGSchemaFactoryImpl";
    private String xmlenc = DEFAULT_ENC;
    protected final static Parser DEFAULT_PARSER = Parser.TEXLIPSE;
    public final static String TRANSFORMER_FACTORY_IMPLEMENTATION =
            "net.sf.saxon.TransformerFactoryImpl";

    String inputenc = DEFAULT_ENC;
    Parser parser = DEFAULT_PARSER;
    private ValidationErrorHandler errorh; //will be lazily initialized

    public BibTeXConverter(){

        /* add default library directories to class path if they exist */
        String fs = File.separator;
        List<String> candidates = new ArrayList<String>();
        candidates.add(System.getProperty("user.dir") + fs + "lib");
        try{
            candidates.add(new File(
                DefaultClassLoaderProvider
                .getRepositoryRootDir(BibTeXConverter.class),
                "lib").getAbsolutePath());
        } catch(Exception ignore){}
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
    }

    /* generating BIBXML from BibTeX */

    /** Converts bibTex from in to BibXML using the current Parser, inputenc, and
     * xmlenc and  writes the result to out. **/
    public void bibTexToXml(File in, File out) throws IOException{
        AbstractBibTeXParser p = null;
        switch(parser){
            case BIB2BIBXML :
                p = new Bib2BibXML(inputenc, xmlenc, cleanInput);;
                break;
            case TEXLIPSE:
                p = new TeXLipseParser(inputenc, xmlenc, cleanInput);
                break;
            default:
                System.err.println("No such parser: "+ parser.toString());
        }

        if(p != null){
            System.out.println("Creating bibxml "+out.getPath());
            System.out.flush();
            p.processFile(in, out);
        }
    }

    public void setBibTeXEncoding(Charset chars){
        inputenc = chars.name();
    }
    
    public void setXMLEncoding(Charset chars){
        xmlenc = chars.name();
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
            throw new IllegalArgumentException("url must end with .xsd or .rng");
        }
        try{
            SchemaFactory sf = SchemaFactory.newInstance(schemaLanguage);
            if(errorh == null){
                errorh = new ValidationErrorHandler();
            }
            xmlValidator = sf.newSchema(schema).newValidator();
            xmlValidator.setErrorHandler(errorh);
        } catch(IllegalArgumentException ex){
            if(schemaLanguage.equals(XMLConstants.RELAXNG_NS_URI)
                && System.getProperty(RELAXNG_SF) == null){
                //try again with JAXP-JARV bridge
                System.out.println("Looking for a JARV RELAX_NG validator.");
                System.setProperty(RELAXNG_SF, JARV_RELAXNG_SF);
                setXMLSchema(schema);
            } else {
                throw new IllegalArgumentException("No service provider found for schema language " + schemaLanguage);
            }
        }
    }
    
    public boolean hasSchema(){
        return xmlValidator != null;
    }
    
    public synchronized boolean validate(File xml) throws SAXParseException, SAXException, IOException{
        boolean result = true;
        if(xmlValidator != null){
            InputStream in = null;
            try{
                in = new BufferedInputStream(new FileInputStream(xml));
                Source src = new SAXSource(new InputSource(in));
                src.setSystemId(xml.toURI().toURL().toString());
                errorh.reset();
                xmlValidator.validate(src);
                result = !errorh.hasError();
            } finally {
                if (in != null){
                    in.close();
                }
            }
        }
        return result;
    }

    public void setBibTeXParser(Parser p){
        parser = p;
    }

    public void createBibXmlDTD(File target) throws IOException{
        copyResourceToFile("bibtexml-strict.dtd", target);
    }
    
    public String getSaxonVersion(){
        String result = null;
        try{
            Class c = Class.forName("net.sf.saxon.Version", true, getClassLoader());
            java.lang.reflect.Method m = c.getMethod("getProductTitle");
            result = (String) m.invoke(null);
        } catch (Exception ignore){}
        return result;
    }

    /* XSLT transformations */    
    /** Converts BibXML using the specified transformer and configuration.
     * The result is optionally converted
     *  to CRLF format. **/
    public synchronized void transform(Transformer t, File input, File output,
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
        
        InputStream in = null;
        OutputStream out = null;
        
        try{
            //open the source xml document
            in = new BufferedInputStream(new FileInputStream(input));
            Source src = new StreamSource(in);
            src.setSystemId(input.toURI().toURL().toString());
            //open the target xml document
            out = new BufferedOutputStream(new FileOutputStream(output));
            if(crlf){
                out = new CRLFOutputStream(out);
            }
            Result res = new StreamResult(out);
            
            t.transform(src, res);
        } finally {
            if(in != null){
                try{
                    in.close();
                } catch (IOException ignore) {}
            }
            if(out != null){
                try{
                    out.close();
                } catch (IOException ignore) {}
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


    /** The method that starts up the BibTeXConverter Application. **/
    public static void main(String[] argv) throws Exception{
        System.setSecurityManager(null);
        LookAndFeelMenu.setLookAndFeel(
            Preferences.userNodeForPackage(BibTeXConverterController.class),
            null);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                java.io.PrintStream syserr = System.err;
                try{
                BibTeXConverterController btcc =
                        new BibTeXConverterController();
                btcc.setVisible(true);
                } catch (Exception ex){
                    System.setErr(syserr);
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        });
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
                }
            }
            if(out != null){
                try{
                    out.close();
                } catch (IOException ignore){
                }
            }
        }
    }

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

    /** The allowed input types. */
    public enum Input {
        BIBXML(".xml"),
        BIBTEX("bib");

        private final String ext;

        Input(String extension){
            ext = extension;
        }

        public String extension(){
            return ext;
        }
    }

    /** The available BibTeX parsers. **/
    public enum Parser {
        TEXLIPSE("texlipse.sf.net"),
        BIB2BIBXML("bibtexml.sf.net");
        private final String longname;

        Parser(String longname){
            this.longname = longname;
        }

        public String toString(){
            return longname;
        }
    }
    
    private static class ValidationErrorHandler implements ErrorHandler{
        private boolean error = false;
        
        public void fatalError( SAXParseException e ) throws SAXException {
            error = true;
            throw e;
        }
        
        public void error( SAXParseException ex ) throws SAXException {
            System.err.print("VALIDATION ERROR at");
            System.err.println(" line " + ex.getLineNumber() + " of " + ex.getSystemId());
            System.err.println("  " + ex.getLocalizedMessage());
            System.err.println();
            error = true;
        }
        
        public void warning( SAXParseException e ) throws SAXException {
            System.err.println("Warning:");
            System.err.println(e);
            
        }
        
        public boolean hasError(){
            return error;
        }
        
        public void reset(){
            error = false;
        }
    }
}