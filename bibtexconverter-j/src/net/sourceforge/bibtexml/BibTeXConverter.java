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
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BibTeXConverter extends XMLConverter{
    private final static boolean CLEAN_INPUT = true;
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
        return new DCMetadata(metadata);
    }

    /**
     * Sets the value of metadata.
     * @param metadata The value to assign metadata.
     */
    public void setMetadata(DCMetadata metadata)
    {
        this.metadata = metadata;
    }

    /** Converts bibTex from in to BibXML using the current Parser, inputenc, and
     * xmlenc and  writes the result to out.
     * @return the number of parse errors that occurred
     **/
    @SuppressWarnings("deprecation")
    public void bibTexToXml(final File in, final File out) throws IOException{
        if(in.equals(out)){
            throw new IOException("Trying to overwrite input file.");
        }
        AbstractBibTeXParser p = null;
        switch(parser){
            case TEXLIPSE:
                p = new TeXLipseParser(inputenc.name(), CLEAN_INPUT);
                break;
            default:
                throw new IOException("No such parser: "+ parser.toString());
        }
        p.setErrorHandler(bibErrorHandler);
        if(p != null){
            Document bibtexml = p.parse(in);
            fillInMetadata();
            bibtexml.getRootElement().addContent(0, metadata.toXML().detachRootElement());
            Format format = Format.getPrettyFormat().setEncoding(getXMLEncoding().name());
            OutputStream outs = new BufferedOutputStream(new FileOutputStream(out));
            (new XMLOutputter(format)).output(bibtexml, outs);
        }
    }

    private void fillInMetadata(){
        metadata.setDate(new java.util.Date());
        metadata.setFormat("application/xml");
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