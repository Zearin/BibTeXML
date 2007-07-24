package de.mospace.lang;

/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
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
 import java.io.Serializable;

/**
 * An immutable String with multiple lines. Since it is not allowed in Java to
 * subclass
 * String I had to use this pretty ugly construct which is nothing more than
 * a wrapper around a String (needed for JTable where cell renderer and cell
 * editor are determined from the column class.)
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 **/
public final class FullTextString implements Serializable{
    private final String me;
    static final long serialVersionUID = 1432403406576665372L;

    /** Constructs a new instance of this class.
     * @param fts the (multi-line) String that will be returned by the toString() method
     * @see #toString()
     **/
    public FullTextString(String fts){
        me = fts;
    }

    /** Clones an existing FullTextString.
     *  @param fts the FullTextString to clone
     **/
    public FullTextString(FullTextString fts){
        me = fts.toString();
    }

    /** Returns a reference to the internal String. Manipulating the returned
     * String will change the state of this object.
     * @return the FullTextString as a String
     **/
    public String toString(){
        return me;
    }

    public boolean equals(Object o){
        if(this == o){
            return true;
        } else if(o instanceof FullTextString){
            return me.equals(o.toString());
        } else {
            return false;
        }
    }

    public int hashCode(){
        return me.hashCode();
    }
}
