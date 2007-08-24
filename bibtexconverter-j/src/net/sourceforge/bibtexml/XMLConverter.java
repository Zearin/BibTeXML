package net.sourceforge.bibtexml;
/*
 * $Id$
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
import java.util.Set;
import java.util.prefs.Preferences;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;
import net.sourceforge.bibtexml.util.XSLTUtils;
import de.mospace.xml.DraconianErrorHandler;

public class XMLConverter{
    private TransformerFactory tf;
    private Validator xmlValidator;
    protected final static Charset DEFAULT_ENC = Charset.defaultCharset();
    protected final static String INTERNAL_PARAMETER_PREFIX = "bibtexml.sf.net.";
    public final static String RELAXNG_SF
        = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;
    public final static String JARV_RELAXNG_SF
        = "org.iso_relax.verifier.jaxp.validation.RELAXNGSchemaFactoryImpl";
    private Charset xmlenc = DEFAULT_ENC;
    protected ErrorHandler saxErrorHandler = DraconianErrorHandler.getInstance();
    private String xmlSchemaID = null;

    public XMLConverter(){
        tf = XSLTUtils.getInstance().tryToGetTransformerFactory();

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

    /** Passing null will re-install the initial
    * {@link de.mospace.xml.DraconianErrorHandler DraconianErrorHandler}
    **/
    public void setValidationErrorHandler(ErrorHandler handler){
        if(handler == null){
            saxErrorHandler = DraconianErrorHandler.getInstance();
        }
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

    public boolean getValidatorFeature(String name) throws SAXNotRecognizedException,
                          SAXNotSupportedException{
        return hasSchema() && xmlValidator.getFeature(name);
    }

    public boolean getTransformerFeature(String name) throws SAXNotRecognizedException,
                          SAXNotSupportedException{
       return tf != null && tf.getFeature(name);
    }

    /**  */
    public void validate(File xml) throws SAXException, IOException{
        InputStream in = null;
        try{
            in = new BufferedInputStream(new FileInputStream(xml));
            Source src = new SAXSource(new InputSource(in));
            src.setSystemId(xml.toURI().toURL().toString());
            validate(src);
        } finally {
            if (in != null){
                in.close();
            }
        }
    }

    /**  */
    public synchronized void validate(Source src) throws SAXException, IOException{
        if(xmlValidator != null){
            xmlValidator.setErrorHandler(saxErrorHandler);
            xmlValidator.validate(src);
        }
    }

    /* XSLT transformations */
    /** Converts BibXML from src to res using the specified transformer and
     * configuration.
     **/
    public static void transform(Transformer t, Source src, Result res,
            Map<String,Object> parameters, String encoding)
            throws TransformerException{

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

        t.transform(src, res);
    }

    /** Converts BibXML using the specified transformer and configuration.
     * The result is optionally converted
     *  to CRLF format. **/
    public static void transform(Transformer t, InputStream in, String systemID, OutputStream out,
            Map<String,Object> parameters, String encoding, boolean crlf)
            throws TransformerException, IOException{

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

        transform(t, src, res, parameters, encoding);
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

    protected synchronized Transformer loadStyleSheet(InputStream in, URL systemId) throws TransformerConfigurationException{
        Transformer t = null;
        if(tf == null){
            tf = XSLTUtils.getInstance().tryToGetTransformerFactory();
        }
        if(tf != null){
            Source styleSrc = new StreamSource(in);
            if(systemId != null){
                styleSrc.setSystemId(systemId.toString());
            }
            t = tf.newTransformer(styleSrc);
        }
        return t;
    }

    /* Helper methods */

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

}