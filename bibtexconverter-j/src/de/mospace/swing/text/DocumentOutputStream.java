/* $Id$
* This class is part of the de.mospace.swing library.
* Copyright (C) 2005-2006 Moritz Ringler
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
package de.mospace.swing.text;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
* An OutputStream that writes to the Document of a Swing text component.
*
* @version $Revision$ ($Date$)
* @author Moritz Ringler
*/
public class DocumentOutputStream extends OutputStream {

    private Charset charset;
    private Document doc;
    private MutableAttributeSet currentStyle;

    /** Sets the foreground color for new output.
    * @param c the new text color
    */
    public void setColor(Color c){
        if(currentStyle == null){
            currentStyle = new SimpleAttributeSet();
        }
        StyleConstants.setForeground (currentStyle, c);
    }

    /**
    * Constructs a new DocumentOutputStream that uses the default system
    * charset for decoding bytes passed to its write methods.
    *
    * @param d the document that this output stream will write to
    *
    * @throws NullPointerException if <code>d</code> is <code>null</code>
    */
    public DocumentOutputStream(Document d) {
        d.getClass(); //throws a null pointer exception if d is null
        charset = Charset.forName(new OutputStreamWriter(this).getEncoding());
        doc = d;
    }

    /**
    * Creates a new DocumentOutputStream that uses the specified
    * charset for decoding bytes passed to its write methods.
    *
    * @param d the document that this output stream will write to
    * @param c charset for decoding bytes passed to one of the write methods
    *
    * @throws NullPointerException if one of the parameters is <code>null</code>
    */
    public DocumentOutputStream(Document d, Charset c) {
        d.getClass(); //throws a null pointer exception if d is null
        c.getClass(); //throws a null pointer exception if c is null
        charset = c;
        doc = d;
    }

    /**
    * Creates a new DocumentOutputStream that uses the
    * charset with the specified name for decoding bytes
    * passed to its write methods.
    *
    * @param d the document that this output stream will write to
    * @param charsetName the name of a charset to use
    *
    * @throws NullPointerException if one of the parameters is <code>null</code>
    * @throws java.nio.charset.UnsupportedCharsetException if the JVM does
    * not support the charset with the specified name
    */
    public DocumentOutputStream(Document d, String charsetName) {
        d.getClass(); //throws a null pointer exception if d is null
        charsetName.getClass(); //throws a null pointer exception if charsetName is null
        charset = Charset.forName(charsetName);
        doc = d;
    }

    /**
    * Sets the character set used for decoding bytes passed to the write
    * methods of this DocumentOutputStream. Has no effect when this
    * OutputStream has been closed.
    *
    * @param name charset for decoding bytes
    *
    * @throws NullPointerException if <code>name</code> is <code>null</code>
    * @throws java.nio.charset.UnsupportedCharsetException if the JVM does
    * not support the charset with the specified name
    */
    public void setCharset(String name) {
        name.getClass();
        if (doc != null) {
            charset = Charset.forName(name);
        }
    }

    /**
    * Sets the character set used for decoding bytes passed to the write
    * methods of this DocumentOutputStream. Has no effect when this
    * OutputStream has been closed.
    *
    * @param c charset for decoding bytes
    *
    * @throws NullPointerException if <code>c</code> is <code>null</code>
    */
    public void setCharset(Charset c) {
        c.getClass();
        if (doc != null) {
            charset = c;
        }
    }

    /**
    * Returns the character set that this DocumentOutputStream uses to write
    * bytes to its document.
    *
    * @return the character set used by this DocumentOutputStream or
    *         <code>null</code> if this stream has been closed
    */
    public Charset getCharset() {
        return charset;
    }

    /**
    * Returns the character set that this DocumentOutputStream uses to write
    * bytes to its document.
    *
    * @return the character set used by this DocumentOutputStream or
    *         <code>null</code> if this stream has been closed
    */
    public String getCharsetName() {
        return (charset == null) ? null : charset.name();
    }

    /**
    * Sets the document that this OutputStream writes to. Has no effect when
    * this OutputStream has been closed.
    *
    * @param d the document that this output stream will write to
    *
    * @throws NullPointerException if <code>d</code> is <code>null</code>
    */
    public void setDocument(Document d) {
        d.getClass();
        if (doc != null) {
            doc = d;
        }
    }

    /**
    * Returns the document that this OutputStream writes to.
    *
    * @return the document that this output stream writes to or
    *         <code>null</code> if this stream has been closed
    */
    public Document getDocument() {
        return doc;
    }

    /**
    * Closes the output stream. A closed stream cannot perform output
    * operations and cannot be reopened.
    */
    public void close() {
        doc = null;
        charset = null;
    }

    /**
    * Inserts the specified byte at the end of the current document of this
    * DocumentOutputStream. <code>b</code> is converted to a unicode
    * character by calling the {@link Charset#decode decode} method of the
    * current charset of this DocumentOutputStream.
    *
    * @param b the byte to write
    * @throws IOException if the output stream has been closed
    */
    public void write(int b) throws IOException {
        write(new byte[]{new Integer(b).byteValue()}, 0, 1);
    }

    /**
    * Inserts the specified bytes at the end of the current document of this
    * DocumentOutputStream. <code>b</code> is converted to a unicode string
    * by calling the {@link Charset#decode decode} method of the current
    * charset of this DocumentOutputStream.<p>
    * <code>write(b)</code> has exactly the same effect as the call
    * <code>write(b, 0, b.length)</code>.
    *
    * @param b the byte to write
    * @throws IOException if the output stream has been closed
    *
    * @see #write(byte[], int, int)
    */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
    * Inserts the specified bytes at the end of the current document of this
    * DocumentOutputStream. <code>b</code> is converted to a unicode string
    * by calling the {@link Charset#decode decode} method of the current
    * charset of this DocumentOutputStream.
    *
    * @param b the data
    * @param off the start offset in the data
    * @param len the number of bytes to write
    * @throws IOException if the output stream has been closed
    * @throws NullPointerException if <code>b</code> is <code>null</code>
    * @throws IndexOutOfBoundsException if the preconditions on the <code>off</code>
    *         and <code>len</code> parameters do not hold
    */
    public void write(byte[] b, int off, int len) throws IOException {
        b.getClass(); //provoke NullPointerException if b is null
        if (doc == null) {
            throw new IOException("Trying to write to a closed output stream.");
        } else {
            append((charset.decode(ByteBuffer.wrap(b, off, len))).toString());
        }
    }

    /**
    * Inserts the specified String at the end of the current document. The
    * document mutation is always done in the
    * AWT Event Dispatching Thread. If the caller thread is not the event
    * dispatching thread, {@link SwingUtilities#invokeLater} will be used.
    *
    * @param s the String to append
    */
    private void append(final String s) {
        try {
            if (EventQueue.isDispatchThread()) {
                doc.insertString(doc.getLength(), s, currentStyle);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        append(s);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}