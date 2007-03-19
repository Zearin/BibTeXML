/*
 * $Id$
 *
 * Copyright (c) 2004-2005 Oskar Ojala
 *
 * Adaptations for BibTeXConverter
 * Copyright (c) 2006 Moritz Ringler (MR)
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

/* MR: disabled to eliminate external dependencies */
//import org.eclipse.jface.text.Position;

/**
 * A class for containing LaTeX references (\label and BibTeX)
 *
 * @author Oskar Ojala
 */
public final class ReferenceEntry extends AbstractEntry {

    /* MR: added to eliminate external dependencies */
    public final static class Position{
        final int docOffset;
        final int length;

        public Position(int off, int len){
            docOffset = off;
            length = len;
        }
    }

    /**
     * A descriptive text of the reference
     */
    public String info;
    /**
     * The end line of the reference declaration (used for BibTeX editing)
     */
    public int endLine;
    /**
     * The document position of the reference declaration (used for BibTeX editing)
     */
    public Position position;

    /**
     * Constructs a new entry with the given key (reference key/name)
     *
     * @param key Reference key
     */
    public ReferenceEntry(String key) {
        this.key = key;
    }

    /**
     * Constructs a new entry with the given key (Reference key/name)
     * and a descriptive text telling something about the reference
     * (used for BibTeX).
     *
     * @param key Reference key
     * @param info A descriptive text of the reference
     */
    public ReferenceEntry(String key, String info) {
        this.key = key;
        this.info = info;
    }

    /**
     * Sets the document position of this entry.
     *
     * @param docOffset Offset from the document start
     * @param length Length of the position
     */
    public void setPosition(int docOffset, int length) {
        this.position = new Position(docOffset, length);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return key;
    }
}
