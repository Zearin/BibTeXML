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
package de.mospace.swing.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import de.mospace.swing.GLOBALS;
import de.mospace.swing.icon.BevelArrowIcon;
import de.mospace.swing.icon.BlankIcon;

/**
* A table header for sortable JTables. It displays an arrow next to the
* column name of the column that is currently used for sorting. Left-Clicking
* on the header of a column triggers sorting of the table by that column.
* The context menu of a column header allows sorting
* the table rows by the values in that column or randomizing the order of the
* rows. If the TableModel of the table implements
* the {@link de.mospace.swing.table.HeaderPopupTableModel} interface the
* elements of the JPopupMenu returned by its {@link
* de.mospace.swing.table.HeaderPopupTableModel#getHeaderPopup(MouseEvent e)
* getHeaderPopup} method are added to the column headers' context menus.
*
* @version $Revision$ ($Date$)
* @author Moritz Ringler
*/
public class SortHeader extends JTableHeader implements TableCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3797288124179411432L;
	private int activeColumn = -1;
    private int popupColumn = -1;
    private final Action sortAction;
    private final Action randomizeAction;
    private transient MouseEvent trigger;
    private final JButton noneButton = new JButton();
    private final ArrowButton arrowButton = new ArrowButton(BevelArrowIcon.DOWN);;

    private static class ArrowButton extends JButton{
        /**
		 * 
		 */
		private static final long serialVersionUID = 6810849957029567699L;
		BevelArrowIcon icon, pressedIcon;

        /** @param initialState one of BevelArrowIcon.UP and BevelArrowIcon.DOWN **/
        public ArrowButton(int initialState){
            super();
            icon = new BevelArrowIcon(initialState, false, false);
            pressedIcon = new BevelArrowIcon(initialState, false, true);
            setIcon(icon);
            setMargin(new Insets(0,0,0,0));
            setHorizontalTextPosition(JButton.LEFT);
        }

        public void setDirection(int d){
            icon.setDirection(d);
            pressedIcon.setDirection(d);
            repaint();
        }
    }

    /** Constructs a new SortHeader associated with the specified JTable.
    * @throws IllegalArgumentException if the TableModel of the table
    * does not implement {@link SortableTableModel}.
    */
    public SortHeader(JTable table) {
        super(table.getColumnModel());
        setTableImpl(table);
        MouseListener ml = new MouseListener();
        addMouseListener(ml);

        noneButton.setHorizontalTextPosition(JButton.CENTER);
        noneButton.setIcon(new BlankIcon());
        noneButton.setMargin(new Insets(0,0,0,0));

        sortAction = new AbstractAction(GLOBALS.getString("Sort")){
            /**
			 * 
			 */
			private static final long serialVersionUID = 1724674134717671381L;

			public void actionPerformed(ActionEvent e){
                sort(popupColumn);
                /* repaint the header */
                JTableHeader header = getTable().getTableHeader();
                header.repaint();
            }
        };

        randomizeAction = new AbstractAction(GLOBALS.getString("Randomize")){
            /**
			 * 
			 */
			private static final long serialVersionUID = 322437783421634633L;

			public void actionPerformed(ActionEvent e){
                randomize();
                /* repaint the header */
                JTableHeader header = getTable().getTableHeader();
                header.repaint();
            }
        };
        setColumnModel(table.getColumnModel());
        setDefaultRenderer(this);
        setReorderingAllowed(false);
    }

    /** @throws IllegalArgumentException if the TableModel of the table
    * does not implement {@link SortableTableModel}.
    */
    public void setTable(JTable table){
        setTableImpl(table);
    }

    /** must do this to avoid bug in Java 6 */
    public void updateUI(){
        setUI((TableHeaderUI)UIManager.getUI(this));
        //updateUI is called by super constructor
        //when noneButton and arrowButton have not been initialized
        if(noneButton != null){
            noneButton.updateUI();
            arrowButton.updateUI();
        }
    }

    private final void setTableImpl(JTable table){
        if (!(table.getModel() instanceof SortableTableModel)){
            throw new IllegalArgumentException("Table is not sortable!"+
                "Its TableModel does not implement the" +
                "de.mospace.swing.table.SortableTableModel interface.");
        }
        super.setTable(table);
        setColumnModel(table.getColumnModel());
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int col) {
        setTable(table);
        JButton button;
        boolean isActive = (col == activeColumn);
        SortableTableModel model = ((SortableTableModel) table.getModel());
        int[] sort = ((SortableTableModel) table.getModel()).getSortOrder()[0];
        if(model.isSorted() && col == sort[0]){
            arrowButton.setDirection(
                    (sort[1] == SortableTableModel.ASCENDING)
                    ? BevelArrowIcon.UP
                    : BevelArrowIcon.DOWN
            );
            button = arrowButton;
        } else {
            button = noneButton;
        }
        button.setText((value == null) ? "" : value.toString());
        button.getModel().setPressed(isActive);
        button.getModel().setArmed(isActive);
        return button;
    }

    private void randomize(){
        SortableTableModel model = (SortableTableModel) getTable().getModel();
        model.randomize();
    }

    private void sort(int col){
        /* Simple Left Click -> Sort by Column */
        int sortCol = getTable().convertColumnIndexToModel(col);
        SortableTableModel model = (SortableTableModel) getTable().getModel();
        model.setSortBy(sortCol);
        model.sort();
    }

    private class MouseListener extends MouseAdapter{
        public void mousePressed(MouseEvent me) {
            /* Left-Click: Sort or Randomize */
            if ((me.getButton() == MouseEvent.BUTTON1)){
                activeColumn = columnAtPoint(me.getPoint());
                if(me.isShiftDown()){
                    randomize();
                } else {
                    sort(activeColumn);
                }
                /* repaint the header */
                JTableHeader header = getTable().getTableHeader();
                // if (getTable().getModel() instanceof AbstractTableModel){
                    // ((AbstractTableModel) getTable().getModel()).fireTableStructureChanged();
                // }
                header.repaint();
                if (header.getTable().isEditing()) {
                    header.getTable().getCellEditor().stopCellEditing();
                }
            /* Right-Click: Show ContextMenu */
            } else if (me.isPopupTrigger()){
                showPopup(me);
            }
        }

        public void showPopup(MouseEvent me){
                popupColumn = columnAtPoint(me.getPoint());
                JPopupMenu popup = new JPopupMenu();
                TableModel model = getTable().getModel();
                if (model instanceof HeaderPopupTableModel){
                    JPopupMenu imported = ((HeaderPopupTableModel) model).getHeaderPopup(me);
                    if(imported != null){
                         Component[] importedItems = imported.getComponents();
                         for(int i=0; i<importedItems.length; i++){
                             if(importedItems[i] instanceof JMenuItem){
                                popup.add((JMenuItem) importedItems[i]);
                            } else if (importedItems[i] instanceof JSeparator){
                                popup.addSeparator();
                            }
                        }
                        popup.addSeparator();
                    }
                }
                popup.add(sortAction);
                popup.add(randomizeAction);
                popup.pack();
                Dimension popupSize = popup.getPreferredSize();
                Toolkit tk = Toolkit.getDefaultToolkit();
                Dimension screen = tk.getScreenSize();
                Insets ins = tk.getScreenInsets(
                    GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration()
                );
                Point scr = new Point(screen.width-ins.right,screen.height-ins.bottom);
                SwingUtilities.convertPointFromScreen(scr, getTable());
                popup.show(me.getComponent(),
                    (int) Math.min(me.getX(),scr.getX()-popupSize.width),
                    (int) Math.min(me.getY(),scr.getY()-popupSize.height)
                );
        }

        public void mouseReleased(MouseEvent me) {
            if ((me.getButton() == MouseEvent.BUTTON1)){
                activeColumn = -1;                // clear
                getTable().getTableHeader().repaint();
            } else if (me.isPopupTrigger()){
                showPopup(me);
            }
        }
    }
}
