/*
 * $Id$
 *
 * Copyright (c) 2004-2005 Oskar Ojala
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
package net.sourceforge.texlipse.model;

/**
 * A superclass for the different types of entries that can occur
 * in LaTeX file, eg. command definitions or reference declarations.
 *
 * This is essentially handled somewhat like a struct in C, due to
 * efficiency resons.
 *
 * @author Oskar Ojala
 */
public abstract class AbstractEntry implements Comparable {

    /**
     * The key (ie. the name) of the entry
     */
    public String key;
    /**
     * The line where the entry is declared
     */
    public int startLine;

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object ae) {
        return key.compareTo(((AbstractEntry) ae).key);
    }

    public boolean equals(AbstractEntry ae) {
        return (key.equals(ae.key));
    }
}
