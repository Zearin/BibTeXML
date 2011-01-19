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
package de.mospace.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;

/** A java icon with a single flat arrow pointing up, left, right, or down.
*
* @version $Revision$ ($Date$)
* @author Moritz Ringler
*/
public class ArrowIcon implements Icon {
    /** Constant for arrows pointing up. */
    public final static int DIRECTION_UP = 1;
    /** Constant for arrows pointing down. */
    public final static int DIRECTION_DOWN = 3;
    /** Constant for arrows pointing left. */
    public final static int DIRECTION_LEFT = 2;
    /** Constant for arrows pointing right. */
    public final static int DIRECTION_RIGHT = 0;
    /** The default size of this icon. */
    private final static int DEFAULT_SIZE = 11;
    
    private int size;
    private int direction;
    
    
    /**
    * Constructs a new arrow icon that has the default size and an
    * arrow that points in the specified direction.
    *
    * @param direction the direction of the arrow. One of DIRECTION_UP,
    * DIRECTION_DOWN, DIRECTION_LEFT, or DIRECTION_RIGHTS.
    */
    public ArrowIcon(int direction){
        init(DEFAULT_SIZE, direction);
    }
    
    
    /**
    * Constructs a new arrow icon with the specified size and an
    * arrow that points in the specified direction.
    *
    * @param size the size for the new icon in pixels
    * @param direction the direction of the arrow. One of DIRECTION_UP,
    * DIRECTION_DOWN, DIRECTION_LEFT, or DIRECTION_RIGHTS
    */
    public ArrowIcon(int size, int direction){
        init(size, direction);
    }
    
    /** Alters the direction of the arrow.
    * @param d the new direction. One of DIRECTION_UP,
    * DIRECTION_DOWN, DIRECTION_LEFT, or DIRECTION_RIGHTS.
    */
    public void setDirection(int d){
        direction = d % 4;
    }
    
    /** Returns the current direction of the arrow.
    * @return the current direction of the arrow. One of DIRECTION_UP,
    * DIRECTION_DOWN, DIRECTION_LEFT, or DIRECTION_RIGHTS.
    */    
    public int getDirection(){
        return direction;
    }
    
    /** Paints this icon on the specified graphics object at the
    * specified location.
    * @param c ignored, may be <code>null</code>
    * @param g the graphics object to paint on
    * @param x the x-offset for this icon
    * @param y the y-offset for this icon
    */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y){
        switch (direction) {
            case DIRECTION_DOWN:
            drawArrow(g, x, y, 180);
            break;
            case DIRECTION_UP:
            drawArrow(g, x, y, 0);
            break;
            case DIRECTION_RIGHT:
            drawArrow(g, x, y, 90);
            break;
            case DIRECTION_LEFT:
            drawArrow(g, x, y, 270);
            break;
            default:
            drawArrow(g, x, y, 90);
            break;
        }
    }
    
    /**
    * Gets the width of this arrow icon.
    *
    * @return the width of this icon
    */
    @Override
    public int getIconWidth(){
        return size;
    }
    
    /**
    * Gets the height of this arrow icon.
    *
    * @return the height of this icon
    */
    @Override
    public int getIconHeight(){
        return size;
    }
    
    
    @SuppressWarnings("hiding")
    private void init(int size, int direction){
        this.size = size;
        this.direction = direction;
    }
    
    
    private void drawArrow(Graphics g, int xo, int yo, int angle){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);
        
        /*
        *  draw the arrow
        */
        // draw GeneralPath (polygon)
        int x1Points[] = {1, 5, 9, 7, 7, 3, 3, 1};
        int y1Points[] = {4, 0, 4, 4, 10, 10, 4, 4};
        GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
        x1Points.length);
        polygon.moveTo(x1Points[0], y1Points[0]);
        for (int index = 1; index < x1Points.length; index++) {
            polygon.lineTo(x1Points[index], y1Points[index]);
        }
        polygon.closePath();
        
        /*
        *  rotate, scale and translate
        */
        AffineTransform arrowt = new AffineTransform();
        arrowt.translate(xo, yo);
        arrowt.scale(size * 1./DEFAULT_SIZE, size * 1./DEFAULT_SIZE);
        double center = DEFAULT_SIZE/2;
        arrowt.rotate(angle * Math.PI/180, center, center);
        
        
        /*
        *  draw the arrow
        */
        AffineTransform savedt = g2.getTransform();
        g2.transform(arrowt);
        g2.draw(polygon);
        g2.setTransform(savedt);
    }
    
}
