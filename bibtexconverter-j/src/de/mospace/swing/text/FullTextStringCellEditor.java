package de.mospace.swing.text;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2003 Moritz Ringler
* $Id: FullTextStringCellEditor.java,v 1.5 2007/02/18 14:19:15 ringler Exp $
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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import de.mospace.lang.FullTextString;

/**
* A multiline string editor for JTables. The cell contents are displayed and
* edited in a text area aligned with the cell's top, left, and
* right bounds. The text area is displayed in the modal layer above the cell
* that is edited.
*
* @version $Revision: 1.5 $ ($Date: 2007/02/18 14:19:15 $)
* @author Moritz Ringler
* @see de.mospace.lang.FullTextString
* @see FullTextStringCellRenderer
**/
public class FullTextStringCellEditor extends AbstractCellEditor
implements TableCellEditor {
    /**The button is used as the primary cell editor. However, ist is nothing
     * but a trigger for dialg which does the actual editing. */
    private ShortButton button = new ShortButton();
    /** A JScrollPane containing a TextArea where the FullTextString
     * is edited. */
    private MyDialog dialg = new MyDialog();

    /** The table one of whose cells is currently edited. **/
    private JTable t;
    /** The row of the cell that is currently edited. **/
    private int row = 0;
    /** The column of the cell that is currently edited. **/
    private int col = 0;


    /**
    * Creates a new FullTextStringCellEditor object.
    */
    public FullTextStringCellEditor() {
        /* set up the button. */
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialg.show(button.getLocationOnScreen(), button.getWidth(),
                button);
            }
        });
    }

    /** Returns the value contained in the editor.
    * @return the last edited value as a FullTextString or <code>null</code>
    *         for empty or invalid values
    * @see de.mospace.lang.FullTextString
    **/
    public Object getCellEditorValue() {
        String content = dialg.getText();
        if (content == null || content.equals("")) {
            return (FullTextString) null;
        } else {
            return new FullTextString(content);
        }
    }

    /**
     * Returns the component that should be added to the client's Component
     * hierarchy. Sets an initial value for the editor. This will cause the
     * editor to stopEditing and lose any partially edited value if the
     * editor is editing when this method is called.
     * Once installed in the client's hierarchy the returned component will
     * be able to draw and receive user input.
     *
     * @param table the JTable that is asking the editor to edit; can be null
     * @param value the value of the cell to be edited, null is a valid value
     * @param isSelected must be true, otherwise <code>null</code> is returned
     * @param row the row of the cell being edited
     * @param column the column of the cell being edited
     * @return a JButton as the primary cell editor
     **/
   public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (dialg.isEditing()){
            cancelCellEditing();
        }

        /* remember the cell that is edited */
        t = table;
        this.row = row;
        col = column;
        if (!isSelected) {
            return null;
        }
        /* store the current value of the cell in the button. */
        button.setValue(value);

        /* return the button as primary cell editor */
        return (Component) button;
    }

    /**
    * Closes the editor, notifies the JTable that editing
    * has cancelled, and returns focus to the JTable and the cell that
    * have been edited.
    */
    public void cancelCellEditing() {
        dialg.stopEditing();
        super.cancelCellEditing();
        selectCell();
    }

    /**
    * Stops editing and returns true to indicate that editing has stopped.
    * Closes the editor, notifies the JTable that editing
    * has stopped, and returns focus to the JTable and the cell that
    * has been edited.
    */
    public boolean stopCellEditing() {
        dialg.stopEditing();
        boolean b = super.stopCellEditing();
        selectCell();
        return b;
    }

    /** Selects the cell from which the editor was called and thus
    * ensures that focus returns to this cell. */
    private void selectCell(){
        if(t != null){
            t.changeSelection(row, col, false, false);
            t.requestFocusInWindow();
        }
    }

    /** Sets the height for the text-area in which cell contents are edited.
    * @param i the new height in pixels, default is 100
    **/
    public void setHeight(int i){
        dialg.setHeight(i);
    }

    /* MYDIALOG CLASS */
    /** The dialog used to edit the multiline-string. **/
    private class MyDialog extends JScrollPane {
        private boolean added = false;
        private boolean isEditing = false;
        private JTextArea myText = new JTextArea("");
        private int height = 100;

        /**
        * Creates a new MyDialog object.
        */
        public MyDialog() {
            setViewportView(myText);
            setVisible(false);
            myText.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    if(isEditing()){
                        stopCellEditing();
                    }
                }
            });

            final Action cancelAction = new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    cancelCellEditing();
                }
            };

            final Action okAction = new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    stopCellEditing();
                }
            };

            myText.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),"cancel");
            myText.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"),"commit");
            myText.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"),"commit");
            myText.getInputMap().put(KeyStroke.getKeyStroke("alt ENTER"),"commit");
            myText.getActionMap().put("commit", okAction);
            myText.getActionMap().put("cancel", cancelAction);
        }

        /**
        * Returns whether this dialog is editing.
        **/
        public boolean isEditing() {
            return isEditing;
        }

        /** Closes this dialog and stops editing. **/
        public void stopEditing(){
            setVisible(false);
            isEditing = false;
        }


        /**
        * Returns the text the user has entered.
        */
        public String getText() {
            return myText.getText();
        }

        /**
        * Sets the height of this dialog.
        * @param i the new dialog height in pixels, default is 100
        **/
        public void setHeight(int i){
            height = i;
            if (isVisible()){
                setBounds(getBounds().x,
                          getBounds().y,
                          getBounds().width,
                          height);
            }
        }


        /**
        * Displays this dialog.
        *
        * @param p position of the top-left corner relative to parent
        * @param w width of the dialog
        * @param c parent component
        */
        public void show(Point p, int w, Component c) {
            isEditing = true;
            JLayeredPane myRoot = ((RootPaneContainer) SwingUtilities.getWindowAncestor(
            c)).getLayeredPane();
            if (!added) {
                myRoot.add(this, JLayeredPane.MODAL_LAYER);
                added = true;
            }
            SwingUtilities.convertPointFromScreen(p, myRoot);
            setBounds(p.x, p.y, w, height);
            myText.setText(button.getValue());
            myText.setCaretPosition(0);
            setVisible(true);
            myText.requestFocusInWindow();
        }
    }

    /* SHORTBUTTON CLASS */
    /**
    * A Button that represents the multi-line string when it is not being
    * edited.
    */
    private class ShortButton extends JButton {

        private String text;

        /**
        * Creates a new ShortButton object.
        */
        public ShortButton() {
            setContentAreaFilled(false);
        }

        /**
        * Sets the value of the ShortButton.
        * @param value the new value
        */
        public void setValue(Object value) {
            text = (value == null) ? null : value.toString();
            super.setText(shorten(text));
        }

        /**
        * Returns the current value of the ShortButton as a String.
        */
        public String getValue() {
            return (text == null) ? "" : text;
        }

        /**
        * Truncates the string str to a single line
        * of at most 29 characters and adds
        * three dots if appropriate.
        */
        private String shorten(String str) {
            if (str == null) {
                return "";
            }
            if (str.equals("")) {
                return "";
            }

            int iend = str.length();
            int inull = str.indexOf(0);
            int ibreak = str.indexOf('\n');
            int i = Math.min(iend, 29);
            if ((inull > -1) && (inull < i)) {
                i = inull;
            }
            if ((ibreak > -1) && ibreak < i) {
                i = ibreak;
            }

            String shortstr = str.substring(0, i);
            if (i < iend) {
                shortstr = shortstr.concat("...");
            }
            return shortstr;
        }
    }
}
