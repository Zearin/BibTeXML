package de.mospace.xml;

/*
 * $Id: XSLParamHandler.java,v 1.1 2007/02/23 14:32:32 ringler Exp $
 * (c) Moritz Ringler, 2006
 *
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/** Extracts top-level stylesheet parameters from an XSL Stylesheet.
* The types of non-string parameters should be declared via
* <code>as="<i>xs</i>:<i>type</i>"</code> where <code><i>type</i></code> is one
* of <code>boolean, integer, float, double</code>, and <code>xs</code>
* is the namespace prefix for the http://www.w3.org/2001/XMLSchema
* namespace.
* Your <code>stylesheet</code> or <code>transform</code> tag could look like
* this
<pre>
<xsl:transform version="2.0"
    [...]
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs [...]">
</pre>
*<p>This class will not
* evaluate complex x-path expressions in <code>select</code> attributes
* or nested elements inside the <code>param</code> element.
*
*/
public class XSLParamHandler extends DefaultHandler{
    private final transient List<XSLParam> xslParam
            = new Vector<XSLParam>();
    transient int level = 0;
    transient XSLParam p;
    transient StringBuilder contents;
    transient String xsPrefix = "";
    public final static String XS_NAMESPACE =
            "http://www.w3.org/2001/XMLSchema";
    public final static String XSL_NAMESPACE =
            "http://www.w3.org/1999/XSL/Transform";
    private final static Pattern selectString = Pattern.compile("'(.*)'");
    private static final Class[] knownTypes = {
        Boolean.class,
        Long.class,
        Float.class,
        Double.class,
        String.class
    };

    private static final Object[] defaults = new Object[]{
        Boolean.FALSE,
        Long.valueOf(0l),
        Float.valueOf(0f),
        Double.valueOf(0.0),
        ""
    };


    private static class XSLParam{
        public Class type = null;
        public Object defaultValue;
        public String name;

        public XSLParam(String name) throws SAXException{
            if(name == null){
                throw new SAXException("Empty parameter name not allowed.");
            }
            this.name = name;
        }

        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("XSLParam ").append(name).append(' ');
            sb.append(String.valueOf(defaultValue)).append(' ');
            sb.append((type == null)? "null" : type.getName());
            return sb.toString();
        }
    }

    public XSLParamHandler(){

    }

    public void startDocument() throws SAXException{
        xslParam.clear();
        level++;
    }

    public void endDocument() throws SAXException{
        level --;
    }

    public void startElement(String uri, String localname, String rawname,
            Attributes atts) throws SAXException{
                if(
                    (level == 2) &&
                    uri.equals(XSL_NAMESPACE) &&
                    "param".equals(localname)
                ){
                    startParam(atts.getValue("name"), atts);
                }
                level ++;
    }

    private void startParam(String name, Attributes atts) throws SAXException{
        p = new XSLParam(name);
        String as = atts.getValue("as");
        if(as != null && xsPrefix != null && as.startsWith(xsPrefix)){
            as = as.substring(xsPrefix.length()); //strip prefix
            if("string".equals(as)){
                p.type = String.class;
            } else if ("boolean".equals(as)){
                p.type = Boolean.class;
            } else if ("double".equals(as)){
                p.type = Double.class;
            } else if ("float".equals(as)){
                p.type = Float.class;
            } else if ("integer".equals(as)){
                p.type = Long.class;
            } else {
                throw new SAXException("Parameter type not supported"
                        + as + " (" + name + ")");
            }
        }
        String select = atts.getValue("select");
        if(select != null){
            /* handle quoted strings */
            Matcher m = selectString.matcher(select);
            if(m.matches()){
                if(p.type == null){
                    p.type = String.class;
                }
                select = m.group(1);
            }

            /* if we have no type, try all known types one after the other */
            if(p.type == null){
                for(Class type : knownTypes){
                    p.defaultValue = makeValue(select, type);
                    if(p.defaultValue != null){
                        p.type = type;
                        break;
                    }
                }
            } else {

                /* else try to make a value for the specified type */
                p.defaultValue = makeValue(select, p.type);
            }
        }
        if(p.type == null){
            /* default to string type */
            p.type = String.class;
        }
    }

    private Object makeValue(String val, Class c){
        Object result = null;
        if(c.equals(String.class)){
            return val;
        }
        if(c.equals(Boolean.class)){
            String vval = val.toLowerCase();
            if("true".equals(vval)){
                return Boolean.TRUE;
            } else if("false".equals(vval)){
                return Boolean.FALSE;
            } else {
                return null;
            }
        }
        try{
            Method m = c.getMethod("valueOf", String.class);
            result = m.invoke(null, val);
        } catch (Exception ignore){
            result = null;
        }
        return result;
    }

    public void endElement(String uri, String localname, String rawname)
            throws SAXException{
        level--;
        if(
            (level == 2) &&
            uri.equals(XSL_NAMESPACE) &&
            "param".equals(localname)
        ){
            endParam();
        }
    }

    private void endParam() throws SAXException{
        if(contents != null){
            String select = contents.toString().trim();
            if(select.length() != 0){
                if(p.defaultValue != null){
                    System.err.println(select);
                    throw new SAXException("xsl:param (" + p.name + ") with select attribute must be empty!");
                }
                p.defaultValue = makeValue(select, p.type);
            }
        }
        if(p.defaultValue == null){
            for(Object o : defaults){
                if(o.getClass().equals(p.type)){
                    p.defaultValue = o;
                }
            }
        }
        xslParam.add(p);
        p = null;
        contents = null;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException{
        if(p != null && level == 3){
            if(contents == null){
                contents = new StringBuilder();
            }
            contents.append(ch, start, length);
        }
    }

    public static void main(String[] argv) throws Exception{
        System.out.println(getStyleSheetParameters(XSLParamHandler.class.getResourceAsStream("bibxml2htmlg.xsl"), null));
    }

    /** For backwards compatibility. This has the same effect as using a null
    EntityResolver. **/
    public static Map<String, Object> getStyleSheetParameters(InputStream in)
    throws SAXException, IOException{
        return getStyleSheetParameters(in, null);
    }

    public static Map<String, Object> getStyleSheetParameters(InputStream in, EntityResolver resolver)
    throws SAXException, IOException
    {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        XSLParamHandler params = new XSLParamHandler();
        reader.setContentHandler(params);
        if(resolver != null){
            reader.setEntityResolver(resolver);
        }
        reader.parse(new InputSource(in));
        return params.getStyleSheetParameters();
    }

    public void startPrefixMapping(String prefix,String uri) throws SAXException{
        if(uri.equals(XS_NAMESPACE)){
            xsPrefix = prefix+":";
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException{
        if(prefix.equals(xsPrefix)){
            xsPrefix = null;
        }
    }

    public Map<String, Object> getStyleSheetParameters(){
        Map<String, Object> result = new TreeMap<String, Object>();
        for(XSLParam pp : xslParam){
            result.put(pp.name, pp.defaultValue);
        }
        return result;
    }
}
