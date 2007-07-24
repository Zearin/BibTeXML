package net.sourceforge.bibtexml;
/*
 * $Id$
 * (c) Moritz Ringler, 2006
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import net.sourceforge.texlipse.model.ParseErrorMessage;
import de.mospace.swing.SortedSetListModel;

/** Provides the component for displaying errors in a structured manner. */
public class ErrorList{
    private final SortedSetListModel model = new SortedSetListModel();
    private final JList list = new JList(model);
    private final JScrollPane ui = new JScrollPane(list);
    private final JLabel title = new JLabel("Errors");
    private XFile file = null;
    private Editor editor; //lazily initialized, see getEditor()
    private boolean allowDoubleClick = true;

    private ErrorAdder errorhandler;
    private final MouseListener editorUpdater = new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() == 2 && allowDoubleClick) {
                final int index = list.locationToIndex(e.getPoint());
                final Object o = model.getElementAt(index);
                if(o instanceof ErrorItem){
                    edit((ErrorItem) o);
                }
            }
        }
    };

    public ErrorList(ActionListener close){
        list.setCellRenderer(new CellRenderer());
        list.addMouseListener(editorUpdater);
        //title.setBackground(Color.RED);
        title.setForeground(Color.RED);
        //title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setOpaque(true);
        if(close == null){
            title.setBorder(BorderFactory.createEtchedBorder());
            ui.setColumnHeaderView(title);
        } else {
            final JPanel box = new JPanel(new BorderLayout());
            final JButton b = new JButton(new ImageIcon(BibTeXConverterController.class.getResource("icon/fileclose.png")));
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setMargin(new Insets(0,0,0,0));
            b.addActionListener(close);
            box.add(b, BorderLayout.WEST);
            box.add(title, BorderLayout.CENTER);
            box.setBorder(BorderFactory.createEtchedBorder());
            ui.setColumnHeaderView(box);
        }
    }

    public synchronized UniversalErrorHandler getErrorHandler(){
        if(errorhandler == null){
            errorhandler = new ErrorAdder();
        }
        return errorhandler;
    }

    private void edit(final ErrorItem item){
        editor = getEditor(); //assure editor is initialized
        if(editor.showFile(file)){
            editor.window().setVisible(true);
            if(item.line != -1){
                editor.goTo(item.line, (item.column == -1)? 0 : item.column);
            }
        }
    }

    public void setAllowDoubleClick(final boolean b){
        allowDoubleClick = b;
    }

    private synchronized Editor getEditor(){
        if(editor == null){
            final Window parent = SwingUtilities.getWindowAncestor(ui);
            editor = new Editor(parent){
                protected boolean save(){
                    final boolean result = super.save();
                    if(result &&
                        parent instanceof BibTeXConverterController &&
                       file != null)
                    {
                        final BibTeXConverterController btcc =
                            (BibTeXConverterController) parent;
                        if(
                            btcc.getInputFile() == null ||
                            (
                               (!btcc.getInputFile().equals(file)) &&
                               askSwitchInput(btcc)
                            )
                        ){
                            btcc.setInputFile(file);
                        }
                    }
                    return result;
                }
            };
            StyleSheetController.placeWindow(editor.window(), parent);
        }
        return editor;
    }

    private boolean askSwitchInput(final JFrame f){
        final int result = JOptionPane.showConfirmDialog(f,
            "Do you want to make this file the input file for BibTeXConverter?",
            "Switch input file?",
            JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    private static class CellRenderer extends Box implements ListCellRenderer{
        private final JLabel line = new JLabel();
        private final JLabel column = new JLabel();
        private final JLabel message = new JLabel();

        public CellRenderer(){
            super(BoxLayout.X_AXIS);
            //setOpaque(true);
            line.setOpaque(true);
            column.setOpaque(true);
            message.setOpaque(true);
            final javax.swing.border.Border b = BorderFactory.createEmptyBorder(0, 5, 0, 3);
            line.setBorder(b);
            column.setBorder(b);
            message.setBorder(b);
            add(line);
            add(Box.createHorizontalStrut(1));
            add(column);
            add(Box.createHorizontalStrut(1));
            add(message);
        }

        public Component getListCellRendererComponent(final JList list,
                                              final Object value,
                                              final int index,
                                              final boolean isSelected,
                                              final boolean cellHasFocus){
            Color fg, bg;

            if(value instanceof ErrorItem){
                final ErrorItem error = (ErrorItem) value;
                line.setText("l. " + ((error.line == -1)? "?" : String.valueOf(error.line)));
                column.setText("c. " + ((error.column == -1)? "?" : String.valueOf(error.column)));
                message.setText(error.message);
            } else {
                line.setText("");
                column.setText("");
                message.setText((value == null)? "" : value.toString());
            }

            bg = ((isSelected)? Color.RED : Color.WHITE);
            fg = ((isSelected)? Color.WHITE : Color.RED);
            column.setForeground(fg);
            column.setBackground(bg);
            line.setForeground(fg);
            line.setBackground(bg);
            message.setForeground(fg);
            message.setBackground(bg);
            return this;
        }
    }

    private final static class ErrorItem implements Comparable{
        /** first line is 1 **/
        public final int line;
        /** first column is 1 **/
        public final int column;
        public final String message;

        public ErrorItem(
            String message, int line, int column){
            this.column = column;
            this.line = line;
            this.message = (message == null)? "" : message; //assure message is non-null
        }

        public static ErrorItem fromSaxParseException(final SAXParseException e){
            return new ErrorItem(
                e.getLocalizedMessage(),
                e.getLineNumber(),
                e.getColumnNumber());
        }

        public static ErrorItem fromParseErrorMessage(final ParseErrorMessage e){
            return new ErrorItem(
                e.getMsg(),
                e.getLine(),
                e.getPos() + 1);
        }

        public static ErrorItem fromTransformerException(final TransformerException e){
            if(e.getLocator() == null){
                if(e.getCause() != null){
                    final Throwable cause = e.getCause();
                    if(cause instanceof SAXParseException){
                        return fromSaxParseException((SAXParseException) cause);
                    } else if(cause instanceof TransformerException){
                        return fromTransformerException((TransformerException) cause);
                    }
                }
            } else {
                return new ErrorItem(
                    e.getLocalizedMessage(),
                    e.getLocator().getLineNumber(),
                    e.getLocator().getColumnNumber());
            }
            return new ErrorItem(e.getLocalizedMessage(), -1, -1);
        }

        @Override
        public String toString(){
            final StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(line)).append(':');
            sb.append(String.valueOf(column)).append(" ");
            sb.append(message);
            return sb.toString();
        }

        @Override
        public boolean equals(final Object o){
            if(this == o){
                return true;
            }
            boolean result = false;
            if(o instanceof ErrorItem){
                final ErrorItem other = (ErrorItem) o;
                result = message.equals(other.message) &&
                    line == other.line &&
                    column == other.column;
            }
            return result;
        }

        @Override
        public int hashCode(){
            int result = 59;
            result = 37 * result + message.hashCode();
            result = 37 * result + line;
            result = 37 * result + column;
            return result;
        }

        /** Error items are compared by line, column, message in this order. */
        public int compareTo(final Object o){
            final ErrorItem other = (ErrorItem) o;
            if(line != other.line){
                return line - other.line;
            } else if (column != other.column){
                return column - other.column;
            } else {
                return message.compareTo(other.message);
            }
        }
    }

    public Component component(){
        return ui;
    }

    private class ErrorAdder implements UniversalErrorHandler{
        public ErrorAdder(){
            //sole constructor
        }

        public void fatalError(final SAXParseException e ) throws SAXException {
            addError(e);
        }

        public void error(final SAXParseException e ) throws SAXException {
            addError(e);
        }

        public void warning(final SAXParseException e ) throws SAXException {
            addError(e);
        }

        public void error(final ParseErrorMessage e) throws IOException {
            addError(e);
        }

        public void fatalError(final TransformerException e ) throws TransformerException {
            addError(e);
        }

        public void error(final TransformerException e ) throws TransformerException {
            addError(e);
        }

        public void warning(final TransformerException e ) throws TransformerException {
            addError(e);
        }

        public void reset(){
            clear();
        }
    }

    private void addError(final TransformerException e){
        addError(ErrorItem.fromTransformerException(e));
    }

    private void addError(final ParseErrorMessage e){
        addError(ErrorItem.fromParseErrorMessage(e));
    }

    private void addError(final SAXParseException e){
        addError(ErrorItem.fromSaxParseException(e));
    }

    private void addError(final ErrorItem item){
        model.add(item);
    }

    public void setFile(final XFile f){
        file = f;
        setTitle((f == null)? "Errors" : "Errors in " + f.getName());
    }

    public void clear(){
        setFile(null);
        model.clear();
    }

    public void setTitle(final String title){
        this.title.setText(title);
    }
}