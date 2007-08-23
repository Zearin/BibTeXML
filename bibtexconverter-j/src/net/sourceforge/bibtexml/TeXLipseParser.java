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
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import net.sourceforge.texlipse.bibparser.Bib2JDomParser;
import net.sourceforge.texlipse.model.ParseErrorMessage;
import org.jdom.Document;

/** A BibTeXParser based on Oscar Ojala's SableCC-generated
* TeXLipse parser that returns the parse result as a JDOM BibTeXML tree.
* @see net.sourceforge.texlipse.bibparser.Bib2JDomParser
**/
public class TeXLipseParser extends AbstractBibTeXParser{
    private Bib2JDomParser parser;
    private boolean hasErrors;

    /** Constructs a new parser that will use the specified charset to read
    * BibTeX input from a byte source such as a file or input stream.
    */
    public TeXLipseParser(String charset)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(charset);
    }

    /** Constructs a new parser that will use the specified charset to read
    * BibTeX input from a byte source such as a file or input stream and that
    * will optionally remove bytes that are not in this charset
    * from the input before trying to decode it.
    * @param cleanInput whether to remove illegal byte sequences from the input
    */
    public TeXLipseParser(String inputChars, boolean cleanInput)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        super(inputChars, cleanInput);
    }

    /** Reads BibTeX from the specified reader and converts it to BibTeXML.
    * If an error handler has been specified this method will use it to
    * handle parse errors. Otherwise an IOException is thrown for the first
    * parse error.
    */
    public Document parse(BufferedReader reader) throws IOException{
        hasErrors = false;
        parser = new Bib2JDomParser(reader);
        ArrayList<ParseErrorMessage> errors = parser.getErrors();
        if(errors != null && !errors.isEmpty()){
            hasErrors = true;
            for(ParseErrorMessage error : errors){
                if(errorhandler == null){
                    throw new IOException(error.toString());
                } else {
                    errorhandler.error(error);
                }
            }
        }
        return parser.getResultDocument();
    }
}