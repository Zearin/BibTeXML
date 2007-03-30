/* $Id: JDoubleList.java,v 1.18 2007/01/17 17:47:58 ringler Exp $
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import de.mospace.swing.icon.ArrowIcon;
import de.mospace.swing.icon.DoubleArrowIcon;

/**
 * A two-column list where items can be moved from one side to the other.
 * The order of the items can also be adjusted.
 *
 * @version $Revision: 1.18 $ ($Date: 2007/01/17 17:47:58 $)
 * @author Moritz Ringler
 */
public class JDoubleList extends JPanel{

    /** Minimum requirements on a model for one of the JLists. **/
    public static interface SingleListModel extends ListModel{
        public Object remove(int index);
        public void add(int index, Object element);
    }

    public static class DefaultSingleListModel extends DefaultListModel implements SingleListModel{
        public DefaultSingleListModel(){
            super();
        }
    }

    public static final int MOVE_RIGHT = 0;
    public static final int MOVE_LEFT  = 1;
    public static final int MOVE_UP    = 2;
    public static final int MOVE_DOWN  = 3;
    public static final int MOVE_START = 4;
    public static final int MOVE_END   = 5;
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    private JList listL, listR;
    JList activeList = null;
    private ScrollPane left, right;
    private JPanel buttonPane;
    private JButton[] buttons;
    private int[] selLeft  = {};
    private int[] selRight = {};
    private JComponent[] compo;
    private boolean symmetric = true;
    private boolean rightOrderFixed = false;
    private boolean leftOrderFixed = false;

    private class ScrollPane extends JScrollPane{
        public ScrollPane(Component c){
            super(c);
        }

        /** This method assures that both scrollpanes always have
            the same size if this DoubleList is symmetric. **/
        public Dimension getPreferredSize(){
            Dimension d = getPreferredIndividualSize();
            if(symmetric){
                Dimension d1 = (this == left)
                    ? right.getPreferredIndividualSize()
                    : left.getPreferredIndividualSize();
                d.height = Math.max(d.height, d1.height);
                d.width = Math.max(d.width, d1.width);
            }
            return d;
        }

        /** This method returns the preferred size of one of the
         scrollpanes. When the list in a scrollpane is empty
         Swing dumbly assings a width of 256 px. We use the dimension
         of the other list and if both are empty a width and height of
         40 px. **/
        public Dimension getPreferredIndividualSize(){
            boolean isLeft = (this == left);
            boolean myListEmpty = isEmpty(isLeft ? listL : listR);
            boolean bothEmpty = isEmpty(listL) && isEmpty(listR);

            return (myListEmpty)
                ? ((bothEmpty)
                        ? new Dimension(40,40)
                        : (isLeft? right : left).getPreferredIndividualSize())
                : super.getPreferredSize();
        }

        private boolean isEmpty(JList list){
            return (list.getModel().getSize() == 0);
        }
    }

    public void setSymmetric(boolean symm){
        symmetric = symm;
    }

    public boolean isSymmetric(){
        return symmetric;
    }

    /** An element of a {@link JDoubleList}. It consists of an object and
    * the information whether this object can be moved from one side of a
    * double list to the other.
    **/
    public static class ListElement{
        private Object obj;
        private boolean mov;

        /** Constructs a new DoubleList element with the
         * given movable property. **/
        public ListElement(Object o, boolean movable){
            obj = o;
            mov = movable;
        }

        /** Returns whether this element can be moved from one side of a
        * double list to the other.
        * @see #setMovable(boolean)
        **/
        public boolean getMovable(){
            return mov;
        }

        /** Returns the object of this ListElement. **/
        public Object getObject(){
            return obj;
        }

        /** Changes the movable property of this double list element.
        * @see #getMovable()
        **/
        public void setMovable(boolean b){
            mov = b;
        }
    }

    /** Constructs a new double list. The SingleListModels in this double
        list will be instances of javax.swing.DefaultListModel. **/
    public JDoubleList(){
        listL = new JList(new DefaultSingleListModel());
        listR = new JList(new DefaultSingleListModel());
        init();
    }

    /** Constructs a new double list from the given single list models. **/
    public JDoubleList(SingleListModel left, SingleListModel right){
        listL = new JList(left);
        listR = new JList(right);
        init();
    }

    /** Constructs a new double List from the given single list models. **/
    public JDoubleList(SingleListModel left, SingleListModel right,
            boolean leftOrderFixed, boolean rightOrderFixed){
        this.leftOrderFixed = leftOrderFixed;
        this.rightOrderFixed = rightOrderFixed;
        listL = new JList(left);
        listR = new JList(right);
        init();
    }

    /** Constructs a new double List from the given single lists. If the list
    * models do not implement SingleListModel an IllegalArgumentException is thrown.
    * @throws IllegalArgumentException if at least one of the list models is not an instance of SingleListModel
    **/
    public JDoubleList(JList left, JList right,
            boolean leftOrderFixed, boolean rightOrderFixed){
        if(!
            (
                (left.getModel() instanceof SingleListModel) &&
                (right.getModel() instanceof SingleListModel)
            )
           ){
               throw new IllegalArgumentException("List models must be instances of SingleListModel.");
        }
        this.leftOrderFixed = leftOrderFixed;
        this.rightOrderFixed = rightOrderFixed;
        listL = left;
        listR = right;
        init();
    }

    private void init(){
        FocusAdapter fa = new FocusAdapter(){
            public void focusGained(FocusEvent e){
                activeList = (JList) e.getSource();
            }
        };
        ActionListener al = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JButton src = (JButton) e.getSource();
                if (src == buttons[MOVE_RIGHT]){
                    moveRight();
                } else if (src == buttons[MOVE_LEFT]){
                    moveLeft();
                } else if (src == buttons[MOVE_UP]){
                    moveUp();
                } else if (src == buttons[MOVE_DOWN]){
                    moveDown();
                } else if (src == buttons[MOVE_START]){
                    moveStart();
                } else if (src == buttons[MOVE_END]){
                    moveEnd();
                }
            }
        };
        listL.addFocusListener(fa);
        listR.addFocusListener(fa);
        left = new ScrollPane(listL);
        right = new ScrollPane(listR);
        buttonPane = new JPanel();

        JButton moveLeft  = new JButton(
                new ArrowIcon(ArrowIcon.DIRECTION_LEFT));
        JButton moveRight = new JButton(
                new ArrowIcon(ArrowIcon.DIRECTION_RIGHT));
        JButton moveUp    = new JButton(
                new ArrowIcon(ArrowIcon.DIRECTION_UP));
        JButton moveDown  = new JButton(
                new ArrowIcon(ArrowIcon.DIRECTION_DOWN));
        JButton moveStart = new JButton(
                new DoubleArrowIcon(DoubleArrowIcon.DIRECTION_UP));
        JButton moveEnd   = new JButton(
                new DoubleArrowIcon(DoubleArrowIcon.DIRECTION_DOWN));
        buttons = new JButton[]{
            moveRight,
            moveLeft,
            moveUp,
            moveDown,
            moveStart,
            moveEnd
        };//do not change the order of the entries!! @see #getButton(int)
        Insets buttonInsets = new Insets(3,3,3,3);

        for (int i=0; i<buttons.length; i++){
            buttons[i].setMargin(buttonInsets);
            buttons[i].addActionListener(al);
        }
        buttonPane.setLayout(new GridLayout(3,2,2,2));
        buttonPane.add(moveLeft);
        buttonPane.add(moveRight);
        buttonPane.add(moveUp);
        buttonPane.add(moveDown);
        buttonPane.add(moveStart);
        buttonPane.add(moveEnd);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        add(left, gbc);

        gbc.gridx = 2;
        add(right, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0,10,0,10);
        gbc.fill = GridBagConstraints.NONE;
        add(buttonPane, gbc);

        compo = new JComponent[]{
            left,
            buttonPane,
            right
        };//do not change the order of the entries!! @see #setComponentSize(int)
    }

    public Component getPart(int part){
        Component result;
        switch(part){
            case LEFT:
                result = left;
                break;
            case CENTER:
                result = buttonPane;
                break;
            case RIGHT:
                result = right;
                break;
            default:
                throw new IllegalArgumentException("Part must be one of LEFT, CENTER, RIGHT.");
        }
        return result;
    }

    /** Moves the selected elements in the src list into the dst list.
    * New items are added after the last selected item or after the last item
    * in the dst list.
    * @param src source list
    * @param dst target list
    **/
    private void move(JList src, JList dst){
        if (src.isSelectionEmpty()){
            return;
        }
        int pos;
        SingleListModel msrc = (SingleListModel) src.getModel();
        SingleListModel mdst = (SingleListModel) dst.getModel();
        if (dst.isSelectionEmpty()){
            pos = mdst.getSize();
        } else {
            pos = dst.getMaxSelectionIndex();
        }
        int[] srcIndices = src.getSelectedIndices();
        Arrays.sort(srcIndices);
        for (int i = srcIndices.length -1; i >= 0; i--){
            Object element = msrc.getElementAt(srcIndices[i]);
            if(
                (element instanceof ListElement)
                    ? ((ListElement) element).getMovable()
                    : true
              ){
                mdst.add(pos,msrc.remove(srcIndices[i]));
            }
        }
    }

    /** Moves the selected elements from the list on the right
    * into the list on the left.
    * New items are added after the last selected item or
    * - if there is no selection - after the last item
    * in the target list.
    **/
    public void moveLeft(){
        move(listR, listL);
    }

    /** Moves the selected elements from the list on the left
    * into the list on the right.
    * New items are added after the last selected item or
    * - if there is no selection - after the last item
    * in the target list.
    **/
    public void moveRight(){
        move(listL, listR);
    }

    private void moveUp(){
        if (
                activeList == null ||
                activeList.isSelectionEmpty() ||
                activeList.getMinSelectionIndex() == 0 ||
                (activeList == listL && leftOrderFixed) ||
                (activeList == listR && rightOrderFixed)
            ){
            return;
        }
        int[] indices = activeList.getSelectedIndices();
        SingleListModel alm = (SingleListModel) activeList.getModel();
        Arrays.sort(indices);
        Object buff = null;
        for (int j=0; j<indices.length;j++){
            buff = alm.remove(indices[j]);
            alm.add(indices[j]-1, buff);
            indices[j]--;
        }
        activeList.setSelectedIndices(indices);
    }

    private void moveDown(){
        SingleListModel alm = (SingleListModel) activeList.getModel();
        if (
                activeList == null ||
                activeList.isSelectionEmpty() ||
                activeList.getMaxSelectionIndex() == alm.getSize()-1 ||
                (activeList == listL && leftOrderFixed) ||
                (activeList == listR && rightOrderFixed)
           ){
            return;
        }
        int[] indices = activeList.getSelectedIndices();
        Arrays.sort(indices);
        Object buff = null;
        for (int j=indices.length-1; j>=0;j--){
            buff = alm.remove(indices[j]);
            alm.add(indices[j]+1, buff);
            indices[j]++;
        }
        activeList.setSelectedIndices(indices);
    }

    private void moveStart(){
        if (activeList == null || activeList.isSelectionEmpty()||
                (activeList == listL && leftOrderFixed) ||
                (activeList == listR && rightOrderFixed)){
            return;
        }
        SingleListModel alm = (SingleListModel) activeList.getModel();
        int[] indices = activeList.getSelectedIndices();
        Arrays.sort(indices);
        Object buff = null;
        for (int j=0; j<indices.length;j++){
            buff = alm.remove(indices[j]);
            alm.add(j, buff);
            indices[j] = j;
        }
        activeList.setSelectedIndices(indices);
    }

    private void moveEnd(){
        if (activeList == null || activeList.isSelectionEmpty() ||
                (activeList == listL && leftOrderFixed) ||
                (activeList == listR && rightOrderFixed)){
            return;
        }
        SingleListModel alm = (SingleListModel) activeList.getModel();
        int[] indices = activeList.getSelectedIndices();
        Arrays.sort(indices);
        Object buff = null;
        int target  = alm.getSize();
        for (int j=indices.length-1; j>=0;j--){
            buff = alm.remove(indices[j]);
            alm.add(--target, buff);
            indices[j] = target;
        }
        activeList.setSelectedIndices(indices);
    }

    /** Returns the JList displayed on the left. **/
    public JList getLeftList(){
        return listL;
    }

    /** Returns the JList displayed on the right. **/
    public JList getRightList(){
        return listR;
    }

    /** Returns one of the buttons that manipulate the double list.
    * @param movexxx one of MOVE_RIGHT, MOVE_LEFT, MOVE_UP, MOVE_DOWN, MOVE_START, MOVE_END
    **/
    public JButton getButton(int movexxx){
        if (movexxx < 0 || movexxx >= buttons.length){
            throw new IllegalArgumentException("Argument must be one of one of"+
                                               "MOVE_RIGHT, MOVE_LEFT, MOVE_UP"+
                                               ", MOVE_DOWN, MOVE_START, "+
                                               "MOVE_END");
        }
        return buttons[movexxx];
    }

    /** Adjusts the size of one of the three double list components.
     * @param comp one of LEFT (list), RIGHT (list), and CENTER (buttons)
     * @param d the new preferred size for the respective component
     **/
     public void setComponentSize(int comp, Dimension d){
         compo[comp].setPreferredSize(d);
         updateUI();
     }
}
