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
import java.net.URI;
import java.util.*;
import javax.swing.*;
import de.mospace.xml.ResettableErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.IOException;
import net.sourceforge.texlipse.model.ParseErrorMessage;

/** Provides the component for displaying errors in a structured manner. */
public class ErrorList{
    private final DefaultListModel model = new DefaultListModel();
    private final JList list = new JList(model);
    private final JScrollPane ui = new JScrollPane(list);
    private final JLabel title = new JLabel("Errors");
    private XFile file = null;
    private Editor editor; //lazily initialized, see getEditor()
    private boolean allowDoubleClick = true;
    
    private ErrorAdder errorhandler;
    private final MouseListener editorUpdater = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && allowDoubleClick) {
                int index = list.locationToIndex(e.getPoint());
                Object o = model.getElementAt(index);
                if(o instanceof ErrorItem){
                    edit((ErrorItem) o);
                }
            }
        }
    };
    
    public ErrorList(){
        list.setCellRenderer(new CellRenderer());
        list.addMouseListener(editorUpdater);
        //title.setBackground(Color.RED);
        title.setForeground(Color.RED);
        //title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(BorderFactory.createEtchedBorder());
        title.setOpaque(true);
        ui.setColumnHeaderView(title);
    }
    
    public synchronized UniversalErrorHandler getErrorHandler(){
        if(errorhandler == null){
            errorhandler = new ErrorAdder();
        }
        return errorhandler;
    }
    
    private void edit(ErrorItem item){
        editor = getEditor(); //assure editor is initialized
        if(editor.showFile(file)){
            editor.window().setVisible(true);
            if(item.line != -1){
                editor.goTo(item.line, (item.column == -1)? 0 : item.column);
            }
        }
    }
    
    public void setAllowDoubleClick(boolean b){
        allowDoubleClick = b;
    }
    
    private synchronized Editor getEditor(){
        if(editor == null){
            Window parent = SwingUtilities.getWindowAncestor(ui);
            editor = new Editor(parent);
            StyleSheetController.placeWindow(editor.window(), parent);
        }
        return editor;
    }
    
    private static class CellRenderer extends Box implements ListCellRenderer{
        private final Dimension lacDimension = new Dimension(50, 0);
        private JLabel line = new JLabel();
        private JLabel column = new JLabel();
        private JLabel message = new JLabel();
        
        public CellRenderer(){
            super(BoxLayout.X_AXIS);
            //setOpaque(true);
            line.setOpaque(true);
            column.setOpaque(true);
            message.setOpaque(true);
            javax.swing.border.Border b = BorderFactory.createEmptyBorder(0, 5, 0, 3);
            line.setBorder(b);
            column.setBorder(b);
            message.setBorder(b);
            add(line);
            add(Box.createHorizontalStrut(1));
            add(column);
            add(Box.createHorizontalStrut(1));
            add(message);
        }
        
        public Component getListCellRendererComponent(JList list,
                                              Object value,
                                              int index,
                                              boolean isSelected,
                                              boolean cellHasFocus){
            Color fg, bg;
            
            if(value instanceof ErrorItem){
                ErrorItem error = (ErrorItem) value;
                StringBuilder sb = new StringBuilder();
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
    
    private final static class ErrorItem{
        /** first line is 1 **/
        public final int line;
        /** first column is 1 **/
        public final int column;
        public final String message;
        
        public ErrorItem(
            String message, int line, int column){
            this.column = column;
            this.line = line;
            this.message = message;
        }
        
        public static ErrorItem fromSaxParseException( SAXParseException e ){
            return new ErrorItem(
                e.getLocalizedMessage(),
                e.getLineNumber(),
                e.getColumnNumber());
        }
        
        public static ErrorItem fromParseErrorMessage( ParseErrorMessage e){
            return new ErrorItem(
                e.getMsg(),
                e.getLine(),
                e.getPos() + 1);
        }
        
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(line)).append(':');
            sb.append(String.valueOf(column)).append(" ");
            sb.append(message);
            return sb.toString();
        }
    }
    
    public Component component(){
        return ui;
    }
    
    private class ErrorAdder implements UniversalErrorHandler{
        public ErrorAdder(){
        }
        
        public void fatalError( SAXParseException e ) throws SAXException {
            addError(e);
        }
        
        public void error( SAXParseException e ) throws SAXException {
            addError(e);
        }
        
        public void warning( SAXParseException e ) throws SAXException {
            addError(e);
        }
        
        public void error(ParseErrorMessage e) throws IOException {
            addError(e);
        }
        
        public void reset(){
            clear();
        }
    }
    
    private void addError(ParseErrorMessage e){
        addError(ErrorItem.fromParseErrorMessage(e));
    }
    
    private void addError(SAXParseException e){
        addError(ErrorItem.fromSaxParseException(e));
    }
    
    private void addError(ErrorItem item){
        model.addElement(item);
    }
    
    public void setFile(XFile f){
        file = f;
        setTitle((f == null)? "Errors" : "Errors in " + f.getName());
    }
    
    public void clear(){
        setFile(null);
        model.clear();
    }
    
    public void setTitle(String title){
        this.title.setText(title);
    }
}