package net.sourceforge.bibtexml.util;
/*
* $Id: XSLTUtils.java 326 2007-08-23 15:19:05Z ringler $
*
* Copyright (c) 2007 Moritz Ringler
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
import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
    A DOM source wrapping a <code>net.sf.saxon.dom.DocumentOverNodeInfo</code>.
    The necessary Saxon classes are loaded using reflection so that they need
    not be on the class path during compile time. Instances of this class will
    be compatible with Transformers constructed using the TransformerFactory
    returned by {@link XSLTUtils#tryToGetTransformerFactory}.
**/
class SaxonDOMSource extends DOMSource implements ReusableSource{
    public final static String DOCUMENT_BUILDER_FACTORY_IMPL =
            "net.sf.saxon.dom.DocumentBuilderFactoryImpl";
    public final static String DOCUMENT_BUILDER_IMPL =
            "net.sf.saxon.dom.DocumentBuilderImpl";

    private static DocumentBuilderFactory factory;

    private SaxonDOMSource(Document d){
        super(d);
    }

    /** Does nothing **/
    public void dispose(){
        //do nothing
    }

    /** Does nothing **/
    public void rewind(){
        //do nothing
    }

    public static SaxonDOMSource newInstance(File xml, ClassLoader cl) throws IOException, SAXException, ParserConfigurationException{
        if(tryToGetSaxonDocumentBuilderFactory(cl) == null){
                return null;
        }
        InputStream xmlstream = new BufferedInputStream(new FileInputStream(xml));
        try{
            InputSource xmlin = new InputSource(xmlstream);
            xmlin.setSystemId(xml.toURI().toURL().toString());
            DocumentBuilder builder = factory.newDocumentBuilder();
            configureDocumentBuilder(builder);
            return new SaxonDOMSource(builder.parse(xmlin));
        } finally {
            xmlstream.close();
        }
    }

    private static void configureDocumentBuilder(DocumentBuilder builder){
        Class clazz = builder.getClass();
        if(DOCUMENT_BUILDER_IMPL.equals(clazz.getName())){
            try{
            TransformerFactory tf = XSLTUtils.getInstance().tryToGetTransformerFactory();
            Class tfclass = tf.getClass();
            Method m = tfclass.getMethod("getConfiguration");
            Object config = m.invoke(tf);
            m = clazz.getMethod("setConfiguration", config.getClass());
            m.invoke(builder, config);
            } catch (Exception ex){
                throw new Error(ex);
            }
        }
    }

    private static synchronized DocumentBuilderFactory tryToGetDocumentBuilderFactory(){
        if(factory == null){
            factory = DocumentBuilderFactory.newInstance();
        }
        return factory;
    }

    private static synchronized DocumentBuilderFactory tryToGetSaxonDocumentBuilderFactory(ClassLoader cl){
        if(factory == null){
            try{
                Class<DocumentBuilderFactory> factoryClass =
                (Class<DocumentBuilderFactory>)
                Class.forName(DOCUMENT_BUILDER_FACTORY_IMPL, true, cl);
                factory = factoryClass.newInstance();
            } catch (Exception ex){
                System.err.println(ex);
                System.err.flush();
            }
        }
        return factory;
    }

}

