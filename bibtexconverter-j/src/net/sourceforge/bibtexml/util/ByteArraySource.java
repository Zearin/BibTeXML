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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import javax.xml.transform.stream.StreamSource;

/** This source caches the contents of a byte source in an internal
    byte array and its getInputStream method will return
    ByteArrayInputStreams constructed on this internal byte array. **/
class ByteArraySource extends StreamSource implements ReusableSource{
    private final byte[] buffer;

    /** Changes to <code>bytes</code> will affect this ByteArraySource. **/
    public ByteArraySource(byte[] bytes){
        buffer = bytes;
        setInputStream(new ByteArrayInputStream(bytes));
    }

    public ByteArraySource(File f) throws IOException{
        super();
        final long n = f.length();
        if(n >= Integer.MAX_VALUE){
            throw new IOException("File is too large.");
        }
        FileChannel xml = new FileInputStream(f).getChannel();
        try{
            ByteBuffer primaryBuffer = ByteBuffer.allocate((int) n);
            xml.read(primaryBuffer);
            buffer = primaryBuffer.array();
        } finally {
            xml.close();
        }
        setSystemId(f.toURI().toURL().toString());
        setInputStream(new ByteArrayInputStream(buffer));
    }

    /** Does nothing **/
    public void dispose(){
        //do nothing
    }

    /** Rewinds this source. **/
    public void rewind(){
        setInputStream(new ByteArrayInputStream(buffer));
    }
}
