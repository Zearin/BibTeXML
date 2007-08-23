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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.xml.validation.*;
import de.mospace.swing.LookAndFeelMenu;
import net.sourceforge.bibtexml.metadata.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BibTeXConverter extends XMLConverter{
    private final static boolean CLEAN_INPUT = true;
    public static final String BIB_NAMESPACE = "http://bibtexml.sf.net/";
    private DCMetadata metadata;

    protected final static Parser DEFAULT_PARSER = Parser.TEXLIPSE;
    private Charset inputenc = XMLConverter.DEFAULT_ENC;
    private Parser parser = DEFAULT_PARSER;
    private BibTeXErrorHandler bibErrorHandler = new ErrorCounter();

    public BibTeXConverter(){
        super();
        metadata = new DCMetadata();
    }

    /**
     * Returns the value of metadata.
     */
    public DCMetadata getMetadata()
    {
        return metadata.createCopy();
    }

    /**
     * Sets the value of metadata.
     * @param metadata The value to assign metadata.
     */
    public void setMetadata(DCMetadata metadata)
    {
        this.metadata = metadata;
    }

    /** Converts bibTex from in to BibXML using the current settings and
    * writes the result to out.
     **/
    public void bibTexToXml(final File in, final File out) throws IOException{
        if(in.equals(out)){
            throw new IOException("Trying to overwrite input file.");
        }
        OutputStream outs = new BufferedOutputStream(new FileOutputStream(out));
        Format format = Format.getPrettyFormat().setEncoding(getXMLEncoding().name());
        (new XMLOutputter(format)).output(bibTexToXml(in), outs);
    }

    /** Converts bibTex from in to BibXML. This method can be used if
    * you want to validate or apply an XSLT transformation to the newly
    * built XML document without writing it to a file and reading it back
    * into memory. The drawback is that validation or transformation errors
    * cannot be located and fixed.<p>
    * Note that the Validator and the Transformer might not
    * support JDOMSource objects; in this case you must convert the
    * JDOM Document to W3C DOM using an org.jdom.output.DOMOutputter
    * and create a DOMSource from the resulting W3C DOM document, e. g.
    * <pre>
    * File input = (new File("bibtex.bib")).getAbsoluteFile();
    * File pseudoXML = new File(input.getParentFile(), "inMemory.xml");
    * String sysID = pseudoXML.toURI().toURL().toString();
    * org.jdom.Document jdom = bibTexToXml(input);
    *
    * bibtexconverter.setXMLSchema(mySchemaURL);
    * boolean supportsJDOM = false;
    * try{
    *   supportsJDOM = bibtexconverter.getValidatorFeature(org.jdom.transform.JDOMSource.JDOM_FEATURE);
    * } catch (Exception ex){
    * }
    * Source src = null;
    * if (supportsJDOM){
    *   src = new org.jdom.transform.JDOMSource(jdom);
    *   src.setSystemID(sysID);
    * } else {
    *   org.w3c.dom.Document w3cdom = (new org.jdom.output.DOMOutputter()).output(jdom);
    *   src = new javax.xml.transform.dom.DOMSource(w3cdom, sysID);
    * }
    * bibtexconverter.validate(src);
    * </pre>
    **/
    @SuppressWarnings("deprecation")
    public Document bibTexToXml(final File in) throws IOException{
        AbstractBibTeXParser p = null;
        switch(parser){
            case TEXLIPSE:
                p = new TeXLipseParser(inputenc.name(), CLEAN_INPUT);
                break;
            default:
                throw new IOException("No such parser: "+ parser.toString());
        }
        p.setErrorHandler(bibErrorHandler);
        Document bibtexml = null;
        if(p != null){
            bibtexml = p.parse(in);
            fillInMetadata();
            Element metanode = new Element("metadata", BIB_NAMESPACE);
            bibtexml.getRootElement().addContent(0, metadata.toXML(metanode));
        }
        return bibtexml;
    }

    private void fillInMetadata(){
        metadata.setDate(new java.util.Date());
        metadata.setFormat("text/xml");
        //if(metadata.getIdentifier() == null){
            metadata.setIdentifier("urn:uuid:" + UUID.randomUUID());
        //}
    }

    public void setBibTeXEncoding(final Charset chars){
        inputenc = chars;
    }

    public Charset getBibTeXEncoding(){
        return inputenc;
    }

    public void setBibTeXErrorHandler(final BibTeXErrorHandler handler){
        bibErrorHandler = handler;
    }

    public void setBibTeXParser(final Parser p){
        parser = p;
    }

    /** The method that starts up the BibTeXConverter Application. **/
    public static void main(final String[] argv) throws Exception{
        System.setSecurityManager(null);
        LookAndFeelMenu.setLookAndFeel(
            Preferences.userNodeForPackage(BibTeXConverterController.class),
            null);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                final java.io.PrintStream syserr = System.err;
                try{
                    (new BibTeXConverterController()).setVisible(true);
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
        TEXLIPSE("texlipse.sf.net");
        private final String longname;

        Parser(String longname){
            this.longname = longname;
        }

        public String toString(){
            return longname;
        }
    }

}