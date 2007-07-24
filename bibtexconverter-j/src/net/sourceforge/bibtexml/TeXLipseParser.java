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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import de.mospace.xml.SaxXMLWriter;
import net.sourceforge.texlipse.bibparser.BibParser2;
import net.sourceforge.texlipse.model.ParseErrorMessage;
import org.xml.sax.SAXException;

public class TeXLipseParser extends AbstractBibTeXParser{
    private BibParser2 parser;
    private boolean hasErrors = false;

    public TeXLipseParser(String charset)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(charset);
    }

    public TeXLipseParser(String inputChars, String outputChars)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(inputChars, outputChars);
    }

    public TeXLipseParser(String inputChars, String outputChars, boolean cleanInput)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(inputChars, outputChars, cleanInput);
    }

    /** does not close stream */
    protected String[] translateBibTeXStream(BufferedReader reader) throws IOException{
        hasErrors = false;
        parser = new BibParser2(reader);
        ArrayList errors = parser.getErrors();
        if(errors != null && !errors.isEmpty()){
            hasErrors = true;
            for(Object error : errors){
                if(errorhandler == null){
                    throw new IOException(error.toString());
                } else {
                    errorhandler.error((ParseErrorMessage) error);
                }
            }
        }
        return new String[0];
    }


    protected void writeBibXML(String[] data, OutputStream out ) throws IOException{
        if(!hasErrors){
            try{
                Properties props = new Properties();
                props.put(OutputKeys.ENCODING, getOutputCharset());
                //props.put(OutputKeys.DOCTYPE_PUBLIC, "-//BibTeXML//DTD XML for BibTeX v1.0//EN");
                //props.put(OutputKeys.DOCTYPE_SYSTEM, "bibtexml-strict.dtd");
                SaxXMLWriter xw = new SaxXMLWriter(out, "http://bibtexml.sf.net/", props);
                parser.printXML(xw);
                xw.close();
            } catch (SAXException ex){
                IOException ioex = new IOException(ex.getMessage());
                ioex.initCause(ex);
                throw ioex;
            }
        }
    }
}