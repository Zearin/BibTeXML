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

import java.awt.Color;

/** A {@link JColorButton} with a title.
 *
 *  @version $Revision$ ($Date$)
 *  @author Moritz Ringler
 */
public class LabeledColorButton extends JColorButton{
    /** (Re-)initializes this button with the given name and color. **/
    protected void init(String name, Color c){
        super.init(name,c);
        setText(name);
    }

    /** Sets the background of this button to c and its foreground
     * to the complementary color of c .**/
    public void setBackground(Color c){
        super.setBackground(c);
        if(c != null){
            setForeground(invert(c));
        }
    }

    private static Color invert(Color c){
        return new Color(255-c.getRed(),255-c.getGreen(),
                255-c.getBlue());
    }

    /** Constructs a new ColorButton with no name and white as its
        associated color. **/
    public LabeledColorButton(){
        super();
    }

    /** Constructs a new ColorButton with the given name and color. **/
    public LabeledColorButton(String name, Color c){
        super(name,c);
    }

    /** Constructs a new ColorButton with the given name and color. **/
    public LabeledColorButton(String name, Color c, boolean foreground){
        super(name,c,foreground);
        setText(name);
    }

   /** Constructs a new ColorButton with the given name and color.
    * @param c the combined RGB components of the color
    * @see java.awt.Color#Color(int rgb)
    **/
    public LabeledColorButton(String name, int c){
        super(name,c);
    }

    /** Constructs a new ColorButton with the given name and white as its
      * associated color. **/
    public LabeledColorButton(String name){
        super(name);
    }
}
