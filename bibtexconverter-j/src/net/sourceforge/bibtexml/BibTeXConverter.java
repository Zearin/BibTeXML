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
import java.net.URI;
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
import javax.xml.transform.URIResolver;
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
import de.mospace.xml.ResettableErrorHandler;

public class BibTeXConverter extends XMLConverter{
    private final static boolean cleanInput = true;
    protected final static Parser DEFAULT_PARSER = Parser.TEXLIPSE;
    private Charset inputenc = XMLConverter.DEFAULT_ENC;
    private Parser parser = DEFAULT_PARSER;
    private BibTeXErrorHandler bibErrorHandler = new ErrorCounter();

    public BibTeXConverter(){
        super();
    }

    /** Converts bibTex from in to BibXML using the current Parser, inputenc, and
     * xmlenc and  writes the result to out.
     * @return the number of parse errors that occurred
     **/
    @SuppressWarnings("deprecation") 
    public void bibTexToXml(File in, File out) throws IOException{
        AbstractBibTeXParser p = null;
        switch(parser){
            case BIB2BIBXML :
                p = new Bib2BibXML(inputenc.name(), getXMLEncoding().name(), cleanInput);
                break;
            case TEXLIPSE:
                p = new TeXLipseParser(inputenc.name(), getXMLEncoding().name(), cleanInput);
                break;
            default:
                throw new IOException("No such parser: "+ parser.toString());
        }
        p.setErrorHandler(bibErrorHandler);
        if(p != null){
            p.processFile(in, out);
        }
    }

    public void setBibTeXEncoding(Charset chars){
        inputenc = chars;
    }
    
    public Charset getBibTeXEncoding(){
        return inputenc;
    }
    
    public void setBibTeXErrorHandler(BibTeXErrorHandler handler){
        bibErrorHandler = handler;
    }
    
    public void setBibTeXParser(Parser p){
        parser = p;
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

}