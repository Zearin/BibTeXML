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
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javax.xml.transform.stream.StreamSource;

class FileSource extends StreamSource implements ReusableSource{
    private final File file;

    public FileSource(File f) throws IOException{
        super();
        file = f;
        setSystemId(f.toURI().toURL().toString());
        setInputStream(new BufferedInputStream(new FileInputStream(f)));
    }

    /** Does nothing **/
    public void dispose() throws IOException{
        InputStream is = getInputStream();
        if(is != null){
            is.close();
        }
    }

    /** Rewinds this source. **/
    public void rewind() throws IOException{
        dispose();
        setInputStream(new BufferedInputStream(new FileInputStream(file)));
    }
}
