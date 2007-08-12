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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.jdom.Document;

public abstract class AbstractBibTeXParser{
    private String inputCharset;
    private boolean cleanInput = false;
    protected BibTeXErrorHandler errorhandler = null;

    public String getInputCharset(){
        return inputCharset;
    }

    public void setInputCharset(final String charset){
        Charset.forName(charset);
        inputCharset = charset;
    }

    public AbstractBibTeXParser(String charset)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        inputCharset = charset;
        //test whether charsets exist
        Charset.forName(charset);
    }

    public AbstractBibTeXParser(String inputChars, boolean cleanInput)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        this(inputChars);
        this.cleanInput = cleanInput;
    }
    
    public Document parse(final File bibTeXIn) throws IOException{
        InputStream ins = null;
        OutputStream outs = null;
        Document result = null;
        Reader a = null;
        try{
            ins = new FileInputStream(bibTeXIn);
            a =  new InputStreamReader(ins, inputCharset);
            if( cleanInput ){
                if(Charset.forName(inputCharset).equals(Charset.forName("ISO-8859-1"))){
                    a = CharFilter.getIsoLatin1Reader(a);
                } else {
                    a = new CharFilter(a, inputCharset);
                }
            }
            result = parse(new BufferedReader(a));
        } finally {
            if(a != null){
                try{
                    a.close();
                } catch (IOException ex){
                    System.err.println(ex);
                    System.err.flush();
                }
            }
        }
        return result;
    }

    public void setErrorHandler(final BibTeXErrorHandler handler){
        errorhandler = handler;
    }

    abstract protected Document parse(BufferedReader reader) throws IOException;
}