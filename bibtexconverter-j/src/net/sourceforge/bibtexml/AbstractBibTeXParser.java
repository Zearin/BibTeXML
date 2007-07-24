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

public abstract class AbstractBibTeXParser{
    private String inputCharset;
    private final String outputCharset;
    private boolean cleanInput = false;
    protected BibTeXErrorHandler errorhandler = null;

    public String getInputCharset(){
        return inputCharset;
    }

    protected String getOutputCharset(){
        return outputCharset;
    }

    public void setInputCharset(final String charset){
        Charset.forName(charset);
        inputCharset = charset;
    }

    public AbstractBibTeXParser(String charset)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        inputCharset = charset;
        outputCharset = charset;
        //test whether charsets exist
        Charset.forName(charset);
    }

    public AbstractBibTeXParser(String inputChars, String outputChars)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        inputCharset = inputChars;
        outputCharset = outputChars;
        //test whether charsets exist
        Charset.forName(inputChars);
        Charset.forName(outputChars);
    }

    public AbstractBibTeXParser(String inputChars, String outputChars, boolean cleanInput)
            throws IllegalCharsetNameException, UnsupportedCharsetException {
        this(inputChars, outputChars);
        this.cleanInput = cleanInput;
    }

    //processFile
    public void processFile(final File bibTeXIn, final File bibXMLOut) throws IOException{
        if(bibTeXIn.equals(bibXMLOut)){
            throw new IOException("Trying to overwrite input file.");
        }
        InputStream ins = null;
        OutputStream outs = null;
        String[] data = null;
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
            data = translateBibTeXStream(new BufferedReader(a));
        } finally {
            if(a != null){
                try{
                    a.close();
                } catch (IOException ex){
                    System.err.println(ex);
                    System.err.flush();
                }
            }
            if(ins != null){
                try{
                    ins.close();
                } catch (IOException ex){
                    System.err.println(ex);
                    System.err.flush();
                }
            }
        }
        if(data != null){
            try{
                outs = new FileOutputStream(bibXMLOut);
                writeBibXML(data, outs);
            } finally {
                if(outs != null){
                    try{
                        outs.close();
                    } catch (IOException ex){
                        System.err.println(ex);
                        System.err.flush();
                    }
                }
            }
        }
    }

    /** does not close stream */
    protected void writeBibXML(final String[] data, final OutputStream out ) throws IOException{
        final StringBuilder header = new StringBuilder(200);
        header.append("<?xml version=\"1.0\" encoding=\""+outputCharset+"\"?>\n");
        //header.append("<!DOCTYPE bibtex:file SYSTEM \"bibtexml-strict.dtd\" >\n");
        header.append("<bibtex:file xmlns:bibtex=\"http://bibtexml.sf.net/\">\n");
        final String footer = "<!-- manual cleanup may be required... -->\n" +
            "</bibtex:file>";
        write(data, header.toString(), footer, out);
    }

    private void write(final String[] lines,
                       final String header,
                       final String footer,
                       final OutputStream out) throws IOException{
        final PrintWriter bw = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(out, outputCharset)), true );
        if(header != null){
            bw.println(header);
        }
        for (String line : lines){
            bw.println(line);
        }
        if(footer != null){
            bw.println(footer);
        }
    }

    public void setErrorHandler(final BibTeXErrorHandler handler){
        errorhandler = handler;
    }

    abstract protected String[] translateBibTeXStream(BufferedReader reader) throws IOException;
}