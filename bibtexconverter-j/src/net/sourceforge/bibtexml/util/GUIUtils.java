package net.sourceforge.bibtexml.util;
/*
 * $Id: GUIUtils.java 322 2007-08-23 10:59:30Z ringler $
 *
 * Copyright (c) 2007 Moritz Ringler
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

import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import static javax.swing.SwingConstants.TOP;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.InputVerifier;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField;
import javax.swing.JComponent;
import java.text.ParseException;

/**
    GUI utility functions.
 **/
public class GUIUtils{
    private static GUIUtils instance;

    private GUIUtils(){
    }

    public static synchronized GUIUtils getInstance(){
        if(instance == null){
            instance = new GUIUtils();
        }
        return instance;
    }

    public Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException("Singleton");
    }

    private boolean snapTo(final Rectangle screen,
            final Rectangle w,
            final Rectangle target,
            final Collection<Rectangle> others,
            final int where)
    {
        Point p = target.getLocation();
        switch(where){
            case RIGHT: p.x += target.width; //right
            break;
            case LEFT: p.x -= w.width; //left
            break;
            case BOTTOM: p.y += target.height; //bottom
            break;
            case TOP: p.y -= w.height; //top
            break;
            default: throw new IllegalArgumentException("Only LEFT, RIGHT, TOP, BOTTOM allowed");
        }
        w.setLocation(p);
        if(screen.contains(w) && !intersects(w, others)){
            return true;
        }
        return false;
    }

    private boolean intersects(Rectangle w, Collection<Rectangle> others){
        for(Rectangle r : others){
            if(w.intersects(r)){
                return true;
            }
        }
        return false;
    }

    public boolean placeWindow(final Window w, final Window owner){
        if(owner == null){
            return false;
        }
        final Set<Window> others = new HashSet<Window>();
        others.addAll(Arrays.asList(owner.getOwnedWindows()));
        others.remove(w);
        others.add(owner);
        final Rectangle sb = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        final Rectangle wb = w.getBounds();
        final Collection<Rectangle> other = new TreeSet<Rectangle>(
        new Comparator<Rectangle>(){
            public int compare(Rectangle a, Rectangle b){
                int result = b.x - a.x;
                if(result == 0){
                    result = - b.y + a.y;
                }
                return result;
            }
        });
        for(Window ww : others){
            if(ww.isVisible()){
                other.add(ww.getBounds());
            }
        }
        final Rectangle ob = owner.getBounds();
        boolean placed = snapTo(sb, wb, ob, other, RIGHT);
        if(!placed){
            for(Rectangle target : other){
                if(
                    !target.equals(ob) &&
                    snapTo(sb, wb, target, other, BOTTOM)
                ){
                    placed = true;
                    break;
                }
            }
        }
        if(!placed){
            for(Rectangle target : other){
                if(snapTo(sb, wb, target, other, RIGHT)){
                    placed = true;
                    break;
                }
            }
        }
        if(placed){
            w.setLocation(wb.getLocation());
        }
        return placed;
    }

    public void installInputVerifier(JFormattedTextField ftf){
        ftf.setInputVerifier(FormattedTextFieldVerifier.INSTANCE);
    }

    private final static class FormattedTextFieldVerifier extends InputVerifier {
        static InputVerifier INSTANCE = new FormattedTextFieldVerifier();

        public FormattedTextFieldVerifier(){
            //sole constructor
        }

        public boolean verify(final JComponent input) {
            if (input instanceof JFormattedTextField) {
                final JFormattedTextField ftf = (JFormattedTextField)input;
                final JFormattedTextField.AbstractFormatter formatter =
                ftf.getFormatter();
                if (formatter != null) {
                    final String text = ftf.getText();
                    try {
                        formatter.stringToValue(text);
                        return true;
                    } catch (ParseException pe) {
                        return false;
                    }
                }
            }
            return true;
        }
        public boolean shouldYieldFocus(final JComponent input) {
            return verify(input);
        }
    }
}
