package de.mospace.swing.text;

/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
 * $Id: FullTextStringInputDialog.java,v 1.6 2007/02/18 14:19:15 ringler Exp $
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import de.mospace.lang.FullTextString;

/**
 * An input dialog for multiline strings.
 * @author Moritz Ringler
 * @version $Revision: 1.6 $ ($Date: 2007/02/18 14:19:15 $)
 * @see de.mospace.lang.FullTextString
 **/
public class FullTextStringInputDialog extends JDialog{
   private String result = null; 
   
   private FullTextStringInputDialog(Frame f,String m, String v){
        super(f, m, true);
        getContentPane().setLayout(new BorderLayout());
        final JTextArea jta = new JTextArea((v == null)? "":v.toString());
        Action okAction = new AbstractAction("OK"){
            public void actionPerformed(ActionEvent e){
                result = jta.getText();
                dispose();
            }
        };
        jta.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"),"commit");
        jta.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"),"commit");
        jta.getInputMap().put(KeyStroke.getKeyStroke("alt ENTER"),"commit");
        jta.getActionMap().put("commit", okAction);
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        JButton ok = new JButton(okAction);
        Action cancelAction = new AbstractAction(
                UIManager.getString("OptionPane.cancelButtonText")){
            public void actionPerformed(ActionEvent e){
                result = null;
                dispose();
            }
        };
        jta.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),"cancel");
        jta.getActionMap().put("cancel", cancelAction);
        final JButton cancel = new JButton(cancelAction);
        getContentPane().add(new JScrollPane(jta), BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.add(ok);
        buttons.add(cancel);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        ((JComponent) getContentPane()).setPreferredSize(new Dimension(500,500));
        pack();
        Dimension dlgSize = getPreferredSize();
        if(f != null){
            Dimension frmSize = f.getSize();
            Point loc = f.getLocation();
            setLocation((frmSize.width - dlgSize.width) / 2 +
                loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
    }
    
    /** Gets the current value of the multi-line string that is or has been
    * edited by this input dialog.
    * @return the current value of the string being edited, or <code>null</code>
    * if the user cancelled the dialog
    */
    private String getValue(){
        return result;
    }
    
    /** Displays a new FullTextStringInputDialog that is initialized with
    * the specified FullTextString.
    * @param owner the parent frame for this dialog, may be <code>null</code>
    * @param message the title for this dialog
    * @param x a FullTextString to initialize the editor with,
    * may be <code>null</code>
    * @return the user input as a FullTextString , or <code>null</code>
    * if the user cancelled the dialog
    */
    static public FullTextString showDialog(Frame owner,
            String message,
            FullTextString x){
        String v = (x == null)? null : x.toString();
        v = showDialog(owner, message, v);
        return (v == null)? null: new FullTextString(v);
    }
    
    /** Displays a new FullTextStringInputDialog that is initialized with
    * the specified string.
    * @param owner the parent frame for this dialog, may be <code>null</code>
    * @param message the title for this dialog
    * @param x a string to initialize the editor with,
    * may be <code>null</code>
    * @return the user input as a string, or <code>null</code>
    * if the user cancelled the dialog
    */    
    static public String showDialog(Frame owner,
            String message,
            String x){
        String v = (x == null)? "" : x;
        FullTextStringInputDialog ftsid = new FullTextStringInputDialog(owner, message, v);
        ftsid.setVisible(true);
        return ftsid.getValue();
    }
}
