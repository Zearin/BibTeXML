/* $Id: FileField.java,v 1.4 2006/04/05 10:49:15 ringler Exp $
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;


/** A titled component that displays a file path that can be modified
*   by means of an attached JFileChooser.
*   This class has considerable overlap with PathInput and its use
*   should be phased out in favour of PathInput.
*
* @version $Revision: 1.4 $ ($Date: 2006/04/05 10:49:15 $)
* @author Moritz Ringler
* @deprecated use PathInput instead
**/
public class FileField extends JPanel {
    private final JLabel file;

    /** Constructs a new FileField with the specified title and initial
    * file path.
    * @param label the title for this FileField
    * @param f the inital file, can be <code>null</code>
    **/
    public FileField(String label, File f){
        setLayout(new BorderLayout());
        JPanel b = new JPanel(new BorderLayout());
        JLabel text       = new JLabel(label);
        text.setBorder(BorderFactory.createEmptyBorder(2,0,5,0));
        file = new JLabel((f==null)?"":f.toString()){
            /* Strangely enough this modified setText
            * leeds to the JLabel assuming its preferredSize
            * That effect seems to depend on the registered
            * propertyChangeListener below
            */
            public void setText(String s){
                super.setText("");
                super.setText(s);
            }
        };
        file.setHorizontalAlignment(JLabel.LEFT);
        file.setOpaque(true);
        file.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 4,Color.white));
        file.setBackground(Color.white);
        b.add(file, BorderLayout.CENTER);
        b.add(new JButton(new AbstractAction(".."){
            public void actionPerformed(ActionEvent e){
                JFileChooser jfc = new JFileChooser();
                try{
                    jfc.setCurrentDirectory(getFile());
                } catch (Exception ex){
                    System.err.println(ex);
                }
                if (jfc.showOpenDialog(FileField.this) == JFileChooser.APPROVE_OPTION){
                    file.setText(jfc.getSelectedFile().getAbsolutePath());
                }
            }
        }), BorderLayout.EAST);
        add(text, BorderLayout.NORTH);
        add(b, BorderLayout.CENTER);
        file.addPropertyChangeListener("text",new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt){
                FileField.this.firePropertyChange(
                evt.getPropertyName(),
                evt.getOldValue(),
                evt.getNewValue()
                );

            }
        });
    }

    /** Sets the file path from a string. If possible {@link #setFile(File)}
    * should be used instead.
    * @param s a string value that will be copied to the file field. It is the
    *        caller's responsibility to assure that it represents a valid path.
    * @see #getText()
    **/
    public void setText(String s){
        file.setText(s);
    }

    /** Sets the file path of the FileField.
    * @param f a file object whose path will be copied to the file field
    * @see #getFile
    **/
    public void setFile(final File f){
        if(f != null){
            setText(f.toString());
        }
    }

    /** Returns the current file path as a string. Consider using
    * <code>String.valueOf(getFile())</code> instead.
    *
    * @return the current file path in string form. It is not guaranteed to
    *         be valid or absolute.
    * @see #setText(String)
    * @see #setFile(File)
    * @see #getFile()
    **/
    public String getText(){
        return file.getText();
    }

   /** Returns the current file path as a file object.
    * @return the current file or <code>null</code> if the
    *         current path is invalid or designates a directory
    * @see #setText(String)
    * @see #setFile(File)
    * @see #getText()
    **/
    public File getFile(){
        try {
            File f = (new File(getText())).getAbsoluteFile();
            if (f.isDirectory()){
                return null;
            }
            return f;
        } catch (Exception ex){
            return null;
        }
    }

}