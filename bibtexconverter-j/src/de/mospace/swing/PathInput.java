package de.mospace.swing;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2006 Moritz Ringler
* $Id: PathInput.java,v 1.9 2007/03/10 17:25:49 ringler Exp $
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;

/**
* @author Moritz Ringler
* @version $Revision: 1.9 $ ($Date: 2007/03/10 17:25:49 $)
*/
public class PathInput extends Box{
    protected JTextField textfield = new JTextField(40);
    private JButton button;
    int selectionMode = JFileChooser.FILES_ONLY;
    private String extension;

    public PathInput(String path, String extension){
        this(path);
        this.extension = extension;
    }

    public PathInput(String path){
        super(BoxLayout.X_AXIS);
        textfield.setText(path);
        textfield.setTransferHandler(new FileTransferHandler(textfield.getTransferHandler()));
        add(textfield);
        add(Box.createHorizontalStrut(2));
        button = new JButton(new AbstractAction(GLOBALS.getString("...")){
            public void actionPerformed(ActionEvent e){
                browse();
            }
        });
        add(button);
        setMaximumSize(getPreferredSize());
    }

    public PathInput(String path, int fileSelectionMode){
        this(path);
        selectionMode = fileSelectionMode;
    }

    protected void browse(){
        String path = getPath();
        if(path.length() == 0){
            path = ".";
        }
        JFileChooser jfc = new JFileChooser(path);
        jfc.setFileSelectionMode(selectionMode);
        if(extension != null){
            jfc.setFileFilter(new FileFilter(){
                public String getDescription(){
                    return extension;
                }

                public boolean accept(File f){
                    return (f != null) && (f.toString().endsWith(extension)) || f.isDirectory();
                }
            }
            );
        }
        try{
            File pwd = new File(getPath());
            if(!pwd.isDirectory()){
                pwd = pwd.getAbsoluteFile().getParentFile();
            }
            if(pwd.isDirectory()){
                jfc.setCurrentDirectory(pwd);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                && jfc.getSelectedFile() != null){
            textfield.setText(jfc.getSelectedFile().getAbsolutePath());
        }
    }

    public void setPath(String p){
        textfield.setText(p);
    }

    public String getPath(){
        return textfield.getText();
    }

    public JTextField getTextfield(){
        return textfield;
    }

    public void setEnabled(boolean on){
        super.setEnabled(on);
        textfield.setEnabled(on);
        button.setEnabled(on);
    }

    private static class FileTransferHandler extends TransferHandler{
        private final TransferHandler parent;

        public FileTransferHandler(TransferHandler parent){
            this.parent =  parent;
        }

        public boolean canImport(JComponent comp, DataFlavor[] flavor){
            boolean result = (comp instanceof JTextField) &&
                Arrays.asList(flavor).contains(DataFlavor.javaFileListFlavor);
            if(! result){
                result = (parent == null)
                    ? super.canImport(comp, flavor)
                    : parent.canImport(comp, flavor);
            }
            return result;
        }

        public int getSourceActions(JComponent comp){
            if(comp instanceof JTextField){
                return COPY_OR_MOVE;
            } else {
                return super.getSourceActions(comp);
            }
        }

        public Transferable createTransferable(JComponent c){
            if(c instanceof JTextField){
                return new StringSelection(((JTextField) c).getSelectedText());
            } else {
                return super.createTransferable(c);
            }
        }

        public boolean importData(JComponent comp, Transferable t){
            if(
                (comp instanceof JTextField) &&
                 t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
            ){
                List data = null;
                try{
                   data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (UnsupportedFlavorException ex){
                    throw new Error(ex);
                } catch (IOException ex){
                    System.err.println(ex);
                    System.err.flush();
                }
                if(data != null && !data.isEmpty()){
                    ((JTextField) comp).setText(((File) data.get(0)).getAbsolutePath());
                    return true;
                }
                return false;
            } else {
                if(parent == null) {
                    return super.importData(comp, t);
                } else {
                    return parent.importData(comp, t);
                }
            }
        }

    }
}