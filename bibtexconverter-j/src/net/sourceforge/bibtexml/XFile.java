package net.sourceforge.bibtexml;
/*
 * $Id: StyleSheetController.java 131 2007-03-19 17:34:53Z ringler $
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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.util.*;
import javax.swing.*;
import org.xml.sax.*;
import java.nio.charset.Charset;
import de.mospace.xml.ResettableErrorHandler;

class XFile extends File {
    private final InputType type;
    private final Charset charset;
    
    public XFile(File f, InputType t, Charset cs){
        super(f.getAbsolutePath()); //NullPointerException if f is null
        t.getClass(); //NullPointerException if t is null
        cs.getClass(); //NullPointerException if cs is null
        type = t;
        charset = cs; 
    }
    
    public final InputType getType(){
        return type;
    }
    
    public final Charset getCharset(){
        return charset;
    }
    
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(o instanceof XFile){
            XFile xf = (XFile) o;
            return super.equals((File) xf) &&
                type == xf.type &&
                charset.equals(xf.charset); 
        }
        return false;
    }
    
    public int hashcode(){
        int result = 59;
        result = 37 * result + super.hashCode();
        result = 37 * result + type.hashCode();
        result = 37 * result + charset.hashCode();
        return result;
    }
}
