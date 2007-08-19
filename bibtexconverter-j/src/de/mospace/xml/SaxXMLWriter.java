package de.mospace.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
/* Sax Events handed to TransformerHandler correspond to features:
namespaces is true and namespace-prefixes is false
see http://www.saxproject.org/namespaces.html
*/
public class SaxXMLWriter{
    /** @deprecated use {@link #getWriter} instead */
    protected final OutputStream out;
    private OutputStreamWriter writer;
    private TransformerHandler hd;
    private AttributesImpl atts;
    private String namesp = "";

    private final Map<String, String> prefixes = new HashMap<String, String>();

    public SaxXMLWriter() throws SAXException{
        this.out = System.out;
        Properties props = new Properties();
        props.put(OutputKeys.ENCODING, "UTF-8");
        props.put(OutputKeys.INDENT, "yes");
        try{
            init(props);
        } catch (UnsupportedEncodingException wonthappen){
            throw new Error(wonthappen);
        }
    }
    
    protected OutputStreamWriter getWriter(){
        return writer; 
    }

    public SaxXMLWriter(OutputStream out, String encoding, String namespace) throws SAXException, UnsupportedEncodingException{
        Properties props = new Properties();
        props.put(OutputKeys.ENCODING, encoding);
        props.put(OutputKeys.INDENT, "yes");
        this.out = out;
        if(namespace != null){
            namesp = namespace;
        }
        init(props);
    }

    public SaxXMLWriter(OutputStream out, String namespace, Properties outputProperties) throws SAXException, UnsupportedEncodingException{
        this.out = out;
        if(namespace != null){
            namesp = namespace;
        }
        init(outputProperties);
    }

    private void init(Properties outputProperties) throws SAXException, UnsupportedEncodingException{
        String enc = (String) outputProperties.get(OutputKeys.ENCODING);
        if(enc == null){
            enc = "UTF-8";
            outputProperties.put(OutputKeys.ENCODING, enc);
        }
        if(outputProperties.get(OutputKeys.INDENT) == null){
            outputProperties.put(OutputKeys.INDENT, "yes");
        }
        StreamResult streamResult = new StreamResult(new OutputStreamWriter(out, enc));
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try{
            tf.setAttribute("indent-number", Integer.valueOf(2));
        } catch (IllegalArgumentException ignore){
        }
        try{
            hd = tf.newTransformerHandler();
        } catch (TransformerConfigurationException ex){
            throw new RuntimeException(ex);
        }
        Transformer serializer = hd.getTransformer();
        //System.err.println("Using "+serializer.getClass().getName());
        serializer.setOutputProperties(outputProperties);
        hd.setResult(streamResult);
        hd.startDocument();
        if(namesp != null){
            hd.startPrefixMapping("", namesp);
        }
        atts = new AttributesImpl();
    }

    /** See <a href="http://www.saxproject.org/namespaces.html">http://www.saxproject.org/namespaces.html</a>  **/
    private String namespace(){
        return namesp;
    }

    private String qName(String localname, String namespace){
        return String.valueOf(prefixes.get(namespace)) + ":" + localname;
    }

    /** See <a href="http://www.saxproject.org/namespaces.html">http://www.saxproject.org/namespaces.html</a>  **/
    public void declarePrefix(String prefix, String namespace) throws SAXException{
        hd.startPrefixMapping(prefix, namespace);
        prefixes.put(namespace, prefix);
    }

    public void emptyElement(String name) throws SAXException {
        hd.startElement(namespace(), name, name, atts);
        hd.endElement(namespace(), name, name);
        atts.clear();
    }

    public void textElement(String name, String text) throws SAXException {
        hd.startElement(namespace(), name, name, atts);
        hd.characters(text.toCharArray(), 0, text.length());
        hd.endElement(namespace(), name, name);
        atts.clear();
    }

    public void emptyElement(String name, String namespace) throws SAXException {
        hd.startElement(namespace, name, qName(name, namespace), atts);
        hd.endElement(namespace, name, qName(name, namespace));
        atts.clear();
    }

    public void startElement(String name) throws SAXException {
        hd.startElement(namespace(), name, name, atts);
        atts.clear();
    }

    public void startElement(String name, String namespace) throws SAXException {
        hd.startElement(namespace, name, name, atts);
        atts.clear();
    }

    public void endElement(String name) throws SAXException {
        hd.endElement(namespace(), name, name);
    }

    public void endElement(String name, String namespace) throws SAXException {
        hd.endElement(namespace, name, name);
    }

    public void attribute(String attr, String value) throws SAXException {
        atts.addAttribute("", attr, attr, "CDATA", value);
    }

    public void attribute(String attr, String value, String namespace) throws SAXException {
        atts.addAttribute(namespace, attr, qName(attr, namespace), "CDATA", value);
    }

    public void text(String text) throws SAXException {
        hd.characters(text.toCharArray(), 0, text.length());
    }

    /** Ends the document without closing the underlying outputstream. **/
    public void endDocument() throws SAXException {
        try{
            out.flush();
            hd.endDocument();
            out.flush();
        } catch (IOException ex){
            throw new SAXException(ex);
        }
    }

    /** Ends the document and closes the underlying outputstream. **/
    public void close() throws SAXException{
        try{
            out.flush();
            hd.endDocument();
            out.flush();
            if(out != System.out && out != System.err){
                out.close();
            }
        } catch (IOException ex){
            throw new SAXException(ex);
        }
    }

}