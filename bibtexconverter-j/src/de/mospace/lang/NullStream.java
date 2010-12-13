package de.mospace.lang;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2006 Moritz Ringler
* $Id$
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

import java.io.IOException;
import java.io.OutputStream;

/** A Singleton OutputStream that serves as a '/dev/null'-like
* data sink.
**/
public final class NullStream extends OutputStream{
    /** The sole instance of this class, which is lazily initialized. */
    private static NullStream instance;

    private NullStream(){
        // hide default constructor.
    }

    /** Returns the sole instance of this class.
    * @return an output stream whose write methods do nothing.
    */
    public static synchronized OutputStream getInstance(){
        if (instance == null){
            instance = new NullStream();
        }
        return instance;
    }

    /** Throws a CloneNotSupportedException. This is a Singleton, therefore
    * clone is not supported.
    * @throws CloneNotSupportedException This method always throws a CloneNotSupportedException.
    */
    @Override
    public Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException();
    }

    /** Does nothing.
    *@throws IOException This method will not throw an Exception.
    */
    @Override
    public void write(int b) throws IOException{
        //Does nothing.
    }

    /** Does nothing.
    *@throws IOException This method will not throw an Exception.
    */
    @Override
    public void write(byte[] b) throws IOException{
        //Does nothing.
    }

    /** Does nothing.
    *@throws IOException This method will not throw an Exception.
    */
    @Override
    public void write(byte[] b, int off, int len) throws IOException{
        //Does nothing.
    }
}