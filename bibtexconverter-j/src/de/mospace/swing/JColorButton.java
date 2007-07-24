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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/** A button for picking colors with a JColorChooser.
 *
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 */
public class JColorButton extends JButton{
    private boolean fg = false;
    Color myColor;
    String title="";
    ActionListener onClick = new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Color c;
            c = JColorChooser.showDialog(JColorButton.this,title, myColor);
            if (c != null){
                myColor = c;
            }
            if(fg){
                setForeground(myColor);
            } else {
                setBackground(myColor);
            }
        }
    };

    /** (Re-)initializes this button with the given name and color. **/
    protected final void init(String name, Color c){
        myColor = c;
        title = name;
        setBackground(myColor);
        addActionListener(onClick);
    }

    /** Constructs a new JColorButton with no name and white as its
        associated color. **/
    public JColorButton(){
        super("      ");
        init("",new Color(255,255,255));
    }

    /** Constructs a new JColorButton with the given name and color. **/
    public JColorButton(String name, Color c){
        super("       ");
        init(name,c);
    }

    /** Constructs a new JColorButton with the given name and color.
    * @param c the combined RGB components of the color
    * @see java.awt.Color#Color(int rgb)
    **/
    public JColorButton(String name, int c){
        super("       ");
        init(name, new Color(c));
    }

    /** Constructs a new JColorButton with the given name and white as its
      * associated color. **/
    public JColorButton(String name){
        super("       ");
        init(name,new Color(255,255,255));
    }

    /** Constructs a new ColorButton with the given name and color. **/
    public JColorButton(String name, Color c, boolean foreground){
        super("       ");
        fg = foreground;
        if(foreground){
            myColor = c;
            title = name;
            setForeground(myColor);
            addActionListener(onClick);
        } else {
            init(name,c);
        }
    }

    /** Returns the color currently associated with this button as a
      * Color object. **/
    public Color getColor(){
        return myColor;
    }

    /** Returns the color currently associated with this button as a
    * combined sRGB int.
    * @see java.awt.Color#getRGB()
    **/
    public int getRGB(){
        return myColor.getRGB();
    }
}
