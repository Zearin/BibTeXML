package net.sourceforge.bibtexml;
/*
 * $Id$
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.jdom.Document;

/** Base class for BibTeX parsers that produce a JDOM BibTeXML tree as a
    parse result. **/
public abstract class AbstractBibTeXParser{
    private String inputCharset;
    private boolean cleanInput = false;
    /** An error handler to use for handling parse errors. */
    protected BibTeXErrorHandler errorhandler = null;

    /** Returns the charset that this parser will use to read
      * BibTeX input from a byte source such as a file or input stream.
      */
    public String getInputCharset(){
        return inputCharset;
    }

    /** Sets the charset that this parser will use to read
      * BibTeX input from a byte source such as a file or input stream.
      */
    public void setInputCharset(final String charset){
        Charset.forName(charset);
        inputCharset = charset;
    }

    /** Constructs a new parser that will use the specified charset to read
      * BibTeX input from a byte source such as a file or input stream.
      */
    public AbstractBibTeXParser(String charset)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        inputCharset = charset;
        //test whether charsets exist
        Charset.forName(charset);
    }

    /** Constructs a new parser that will use the specified charset to read
      * BibTeX input from a byte source such as a file or input stream and that
      * will optionally remove characters that are not in this charset
      * from the input before trying to decode it.
      * @param cleanInput whether to remove illegal characters from the input
      */
    public AbstractBibTeXParser(String inputChars, boolean cleanInput)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        this(inputChars);
        this.cleanInput = cleanInput;
    }

    /** Reads BibTeX from the specified input stream and converts it to bibTeXML.
    * The stream is not closed by this method.
    */
    public Document parse(final InputStream ins) throws IOException{
        Reader a = new InputStreamReader(ins, inputCharset);
        if( cleanInput ){
            if(Charset.forName(inputCharset).equals(Charset.forName("ISO-8859-1"))){
                a = CharFilter.getIsoLatin1Reader(a);
            } else {
                a = new CharFilter(a, inputCharset);
            }
        }
        return parse(new BufferedReader(a));
    }

    /** Reads BibTeX from the specified file and converts it to bibTeXML.
    * The input file is closed before this method returns.
    */
    public Document parse(final File bibTeXIn) throws IOException{
        InputStream ins = new FileInputStream(bibTeXIn);
        try{
            return parse(ins);
        } finally {
            ins.close();
        }
    }

    /** Sets the error handler to use for handling parse errors. */
    public void setErrorHandler(final BibTeXErrorHandler handler){
        errorhandler = handler;
    }

    /** Reads BibTeX from the specified reader and converts it to BibTeXML.*/
    abstract public Document parse(BufferedReader reader) throws IOException;
}