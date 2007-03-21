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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** This FilterOutputStream replaces single linefeeds (LF, '\n') with 
 * linefeed + carriage returns (CRLF, '\n\r'). Does not work for utf-16
 * encoding, may not work with utf-8 **/
public class CRLFOutputStream extends FilterOutputStream{
    int lastb;
    final private static int LF = '\n' & 0xFF;
    final private static int CR = '\r' & 0xFF;

    public CRLFOutputStream(OutputStream outs){
        super(outs);
    }

    public void write(int b) throws IOException{
        if(b == LF && lastb != CR){
            out.write(CR);
        }
        out.write(b);
        lastb = b;
    }

    public void write(byte[] b, int off, int len) throws IOException{
        final int limit = off + len;
        for(int i = off; i < limit; i++){
            write(b[i]);
        }
    }
}