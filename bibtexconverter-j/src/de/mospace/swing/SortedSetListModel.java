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
package de.mospace.swing;

import javax.swing.*;
import java.util.*;


/** Wraps a sorted set as a ListModel.
*
* @version $Revision$ ($Date$)
* @author Moritz Ringler
**/
public class SortedSetListModel<T> extends AbstractListModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4418159824081831084L;
	private final SortedSet<T> sortedSet;

    public SortedSetListModel(){
        sortedSet = new TreeSet<T>();
    }

    public SortedSetListModel(SortedSet<T> s){
        sortedSet = s;
    }

    @Override
    public int getSize(){
        return sortedSet.size();
    }

    @Override
    public synchronized Object getElementAt(int index){
        if(index < 0 || index >= getSize()){
            throw new ArrayIndexOutOfBoundsException(index);
        }
        Iterator<T> it = sortedSet.iterator();
        for(int i=0; i<index; i++){
            it.next();
        }
        return it.next();
    }

    public synchronized void add(T element){
        if(sortedSet.add(element)){
            fireContentsChanged(this, 0, getSize());
        }
    }

    public synchronized void remove(T element){
        if(sortedSet.remove(element)){
            fireContentsChanged(this, 0, getSize());
        }
    }

    public synchronized void clear(){
       sortedSet.clear();
       fireContentsChanged(this, 0, getSize());
    }

}