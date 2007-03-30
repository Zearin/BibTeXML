/* $Id: FileField.java,v 1.4 2006/04/05 10:49:15 ringler Exp $
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

import javax.swing.*;
import java.util.*;


/** Wraps a sorted set as a ListModel.
*
* @version $Revision: 1.4 $ ($Date: 2006/04/05 10:49:15 $)
* @author Moritz Ringler
**/
public class SortedSetListModel extends AbstractListModel {
    private final SortedSet set;
    
    public SortedSetListModel(){
        set = new TreeSet();
    }

    public SortedSetListModel(SortedSet s){
        set = s;
    }
    
    public int getSize(){
        return set.size();
    }
    
    public synchronized Object getElementAt(int index){
        if(index < 0 || index >= getSize()){
            throw new ArrayIndexOutOfBoundsException(index);
        }
        Iterator it = set.iterator();
        for(int i=0; i<index; i++){
            it.next();
        }
        return it.next();
    }
    
    public synchronized void add(Object element){
        if(set.add(element)){ 
            fireContentsChanged(this, 0, getSize());
        }
    }
    
    public synchronized void remove(Object element){
        if(set.remove(element)){
            fireContentsChanged(this, 0, getSize());
        }
    }
    
    public synchronized void clear(){
       final int n = getSize();
       set.clear();
       fireContentsChanged(this, 0, getSize());
    }

}