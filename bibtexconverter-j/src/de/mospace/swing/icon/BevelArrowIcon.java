package de.mospace.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.UIManager;

/** A java icon with a triangular shadowed arrow pointing up or down.
* Original package: jp.gr.java_conf.tame.swing.icon
* This class is in the public domain.
*
* @version $Revision$ ($Date$)
*/
public class BevelArrowIcon implements Icon {
    /** Constant for arrows pointing up. */
    public static final int UP    = 0;

    /** Constant for arrows pointing down. */
    public static final int DOWN  = 1;

    /** The default size of this icon. */
    private static final int DEFAULT_SIZE = 11;

    private Color edge1;
    private Color edge2;
    private Color fill;
    private int size;
    private int direction;

    /**
    * Constructs a default size bevel arrow icon with the specified style
    * and an arrow that points in the specified direction.
    *
    * @param direction the direction of the arrow. One of DIRECTION_UP,
    * DIRECTION_DOWN
    * @param isRaisedView whether the icon is raised
    * @param isPressedView whether the icon is pressed
    */
    public BevelArrowIcon(int direction, boolean isRaisedView, boolean isPressedView) {
        if (isRaisedView) {
            if (isPressedView) {
                init( UIManager.getColor("controlLtHighlight"),
                UIManager.getColor("controlDkShadow"),
                UIManager.getColor("controlShadow"),
                DEFAULT_SIZE, direction);
            } else {
                init( UIManager.getColor("controlHighlight"),
                UIManager.getColor("controlShadow"),
                UIManager.getColor("control"),
                DEFAULT_SIZE, direction);
            }
        } else {
            if (isPressedView) {
                init( UIManager.getColor("controlDkShadow"),
                UIManager.getColor("controlLtHighlight"),
                UIManager.getColor("controlShadow"),
                DEFAULT_SIZE, direction);
            } else {
                init( UIManager.getColor("controlShadow"),
                UIManager.getColor("controlHighlight"),
                UIManager.getColor("control"),
                DEFAULT_SIZE, direction);
            }
        }
    }

    /**
    * Constructs a new bevel arrow icon with the specified colors, size
    * and orientation.
    *
    * @param direction the direction of the arrow. One of DIRECTION_UP,
    * DIRECTION_DOWN
    * @param edge1 the hihglight color for the icon edge
    * @param edge2 the shadow color for the icon edge
    * @param fill the background color for the icon
    * @param size the size in pixels for the icon
    */
    public BevelArrowIcon(Color edge1, Color edge2, Color fill,
    int size, int direction) {
        init(edge1, edge2, fill, size, direction);
    }

    /** @param d one of UP or DOWN **/
    public void setDirection(int d){
        if(d!=UP && d!= DOWN){
            throw new IllegalArgumentException();
        }
        direction = d;
    }

    /** Paints this icon on the specified graphics object at the
    * specified location.
    * @param c ignored, may be <code>null</code>
    * @param g the graphics object to paint on
    * @param x the x-offset for this icon
    * @param y the y-offset for this icon
    */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        switch (direction) {
            case DOWN: drawDownArrow(g, x, y); break;
            default: drawUpArrow(g, x, y);   break;
        }
    }

    /**
    * Gets the width of this bevel arrow icon.
    *
    * @return the width of this icon
    */
    public int getIconWidth() {
        return size;
    }

    /**
    * Gets the height of this bevel arrow icon.
    *
    * @return the height of this icon
    */
    public int getIconHeight() {
        return size;
    }


    private void init(Color edge1, Color edge2, Color fill,
    int size, int direction) {
        this.edge1 = edge1;
        this.edge2 = edge2;
        this.fill = fill;
        this.size = size;
        this.direction = direction;
    }

    private void drawDownArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        g.drawLine(xo, yo,   xo+size-1, yo);
        g.drawLine(xo, yo+1, xo+size-3, yo+1);
        g.setColor(edge2);
        g.drawLine(xo+size-2, yo+1, xo+size-1, yo+1);
        int x = xo+1;
        int y = yo+2;
        int dx = size-6;
        while (y+1 < yo+size) {
            g.setColor(edge1);
            g.drawLine(x, y,   x+1, y);
            g.drawLine(x, y+1, x+1, y+1);
            if (0 < dx) {
                g.setColor(fill);
                g.drawLine(x+2, y,   x+1+dx, y);
                g.drawLine(x+2, y+1, x+1+dx, y+1);
            }
            g.setColor(edge2);
            g.drawLine(x+dx+2, y,   x+dx+3, y);
            g.drawLine(x+dx+2, y+1, x+dx+3, y+1);
            x += 1;
            y += 2;
            dx -= 2;
        }
        g.setColor(edge1);
        g.drawLine(xo+(size/2), yo+size-1, xo+(size/2), yo+size-1);
    }

    private void drawUpArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        int x = xo+(size/2);
        g.drawLine(x, yo, x, yo);
        x--;
        int y = yo+1;
        int dx = 0;
        while (y+3 < yo+size) {
            g.setColor(edge1);
            g.drawLine(x, y,   x+1, y);
            g.drawLine(x, y+1, x+1, y+1);
            if (0 < dx) {
                g.setColor(fill);
                g.drawLine(x+2, y,   x+1+dx, y);
                g.drawLine(x+2, y+1, x+1+dx, y+1);
            }
            g.setColor(edge2);
            g.drawLine(x+dx+2, y,   x+dx+3, y);
            g.drawLine(x+dx+2, y+1, x+dx+3, y+1);
            x -= 1;
            y += 2;
            dx += 2;
        }
        g.setColor(edge1);
        g.drawLine(xo, yo+size-3,   xo+1, yo+size-3);
        g.setColor(edge2);
        g.drawLine(xo+2, yo+size-2, xo+size-1, yo+size-2);
        g.drawLine(xo, yo+size-1, xo+size, yo+size-1);
    }
}
