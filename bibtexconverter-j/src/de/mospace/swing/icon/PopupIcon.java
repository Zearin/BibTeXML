/*
*  $Id: PopupIcon.java,v 1.4 2007/02/18 14:20:23 ringler Exp $
*  This class is part of the de.mospace.swing library.
*  Copyright (C) 2005-2006 Moritz Ringler
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU General Public License
*  as published by the Free Software Foundation; either version 2
*  of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package de.mospace.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;

/** A Java icon with a black triangle pointing downwards, similar to the
* combo box control for displaying the combo box selection.
* @author Moritz Ringler
* @version $Revision: 1.4 $ ($Date: 2007/02/18 14:20:23 $)
*/
public class PopupIcon implements Icon {
    /** The default size of this icon. */
    private final static int DEFAULT_SIZE = 10;

    private int size = DEFAULT_SIZE;


    /**
    * Constructs a new PopupIcon that has the default size.
    *
    */
    public PopupIcon() {
        //sole constructor
    }


    /**
    * Constructs a new PopupIcon with the specified size.
    *
    * @param size the width and height of the new icon
    */
    public PopupIcon(int size) {
        this.size = size;
    }


    /** Paints this icon on the specified graphics object at the
    * specified location.
    * @param c ignored, may be <code>null</code>
    * @param g the graphics object to paint on
    * @param xo the x-offset for this icon
    * @param yo the y-offset for this icon
    */
    public void paintIcon(Component c, Graphics g, int xo, int yo) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);

        /*
        *  draw the arrow
        */
        // draw GeneralPath (polygon)
        int x1Points[] = {0, 9, 5, 4, 0};
        int y1Points[] = {0, 0, 9, 9, 0};
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
        arrowt.scale(size * 1. / DEFAULT_SIZE, size * 1. / DEFAULT_SIZE);
        //double center = DEFAULT_SIZE / 2;

        /*
        *  draw the arrow
        */
        AffineTransform savedt = g2.getTransform();
        g2.transform(arrowt);

        g2.setPaint(Color.black);
        g2.fill(polygon);
        g2.draw(polygon);
        g2.setTransform(savedt);

    }

    /**
    * Gets the width of this icon.
    *
    * @return the width of this icon
    */
    public int getIconWidth() {
        return size;
    }


   /**
    * Gets the height of this icon.
    *
    * @return the height of this icon
    */
    public int getIconHeight() {
        return size;
    }

}
