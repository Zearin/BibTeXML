package de.mospace.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/** A blank Java icon. 
* Original package: jp.gr.java_conf.tame.swing.icon
* This class is in the public domain.
* @version 1.0 02/26/99
*/
public class BlankIcon implements Icon {
    private Color fillColor;
    private int size;
    
    /** Creates a transparent blank icon that is 11 px wide
    * and 11 px high.*/
    public BlankIcon() {
        this(null, 11);
    }
    
    /** Creates a new blank icon with the specified color and size.
    * @param color the background color for the icon, if it is null the
    * icon will be transparent
    * @param size the size in pixels for the icon
    */
    public BlankIcon(Color color, int size) {
        fillColor = color;
        
        this.size = size;
    }
    
    /** Paints this icon on the specified graphics object at the
    * specified location.
    * @param c ignored, may be <code>null</code>
    * @param g the graphics object to paint on
    * @param x the x-offset for this icon
    * @param y the y-offset for this icon
    */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (fillColor != null) {
            g.setColor(fillColor);
            g.drawRect(x, y, size-1, size-1);
        }
    }
    
    /**
    * Gets the width of this icon.
    *
    * @return the width of this icon
    */
    @Override
    public int getIconWidth() {
        return size;
    }
    
    /**
    * Gets the height of this icon.
    *
    * @return the height of this icon
    */
    @Override
    public int getIconHeight() {
        return size;
    }
}
