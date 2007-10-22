/* $Id: FileField.java 12 2007-07-27 09:48:53Z ringler $
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
package de.mospace.swing;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter{
    private String descr;
    private String[] ext;

    public ExtensionFileFilter(String description, String[] extensions){
        descr = description;
        ext = extensions;
        if(ext != null && ext.length == 0){
            ext = null;
            if(descr == null){
                descr = "*";
            }
        }
        if(descr == null){
            StringBuffer sb = new StringBuffer();
            for(int i=0; i<ext.length; i++){
                sb.append('*').append(ext[i]).append(' ');
            }
            descr = sb.toString();
        }
    }

    public boolean accept(File f){
        if(ext == null || f.isDirectory()){
            return true;
        }
        String name = f.getName();
        for(int i=0; i<ext.length; i++){
            if(name.endsWith(ext[i])){
                return true;
            }
        }
        return false;
    }

    public String getDescription(){
        return descr;
    }
}