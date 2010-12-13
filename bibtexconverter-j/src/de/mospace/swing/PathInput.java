package de.mospace.swing;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2006 Moritz Ringler
* $Id$
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
import java.awt.event.ActionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;
import java.util.logging.Logger;

/**
* @author Moritz Ringler
* @version $Revision$ ($Date$)
*/
public class PathInput extends Box{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4396922544652610241L;
	private static final Logger logger = Logger.getLogger(PathInput.class.getPackage().getName());
    protected JTextField textfield = new JTextField(40);
    private JButton button;
    int selectionMode = JFileChooser.FILES_ONLY;
    private String extension;

    /*
     *  Event generation and listener management
     */
    private EventListenerList listenerList = new EventListenerList();
    private transient ActionEvent event = null;
    private String actionCommand = null;

    public PathInput(String path, String extension){
        this(path);
        this.extension = extension;
    }

    public PathInput(String path){
        super(BoxLayout.X_AXIS);
        textfield.setText(path);
        textfield.setTransferHandler(new FileTransferHandler(textfield.getTransferHandler()));
        textfield.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    event = e;
                    PathInput.this.fireActionPerformed();
                }
        });
        add(textfield);
        add(Box.createHorizontalStrut(2));
        button = new JButton(new AbstractAction(GLOBALS.getString("...")){
            /**
			 * 
			 */
			private static final long serialVersionUID = 178269836460842899L;

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
            jfc.setFileFilter(new ExtensionFileFilter(null,
                extension.replaceAll("\\*", "").split("\\s+")));
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
            fireActionPerformed();
        }
    }

    public void setPath(String p){
        textfield.setText(p);
        fireActionPerformed();
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

    public void addActionListener(ActionListener a){
        listenerList.add(ActionListener.class, a);
    }

    public void removeActionListener(ActionListener a){
        listenerList.remove(ActionListener.class, a);
    }

    private String getActionCommand(){
        return (actionCommand == null)? textfield.getText() : actionCommand;
    }

    public void setActionCommand(String s){
        actionCommand = s;
        textfield.setActionCommand(s);
    }

    /**  Informs listeners that something has changed. */
    protected void fireActionPerformed() {
        Object[] listeners = listenerList.getListenerList();

        /*
        *  Process the listeners last to first, notifying
        *  those that are interested in this event
        */
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                if(event == null){
                    event = new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED,
                        getActionCommand());
                }
                ((ActionListener) listeners[i + 1]).actionPerformed(event);
            }
        }
        event = null;
    }

    private class FileTransferHandler extends TransferHandler{
        /**
		 * 
		 */
		private static final long serialVersionUID = -7066260039944019815L;
		private final TransferHandler parent;

        public FileTransferHandler(TransferHandler parent){
            this.parent =  parent;
        }

        public boolean canImport(JComponent comp, DataFlavor[] flavor){
            boolean result =
                Arrays.asList(flavor).contains(DataFlavor.javaFileListFlavor);
            if(! result){
                result = (parent == null)
                    ? super.canImport(comp, flavor)
                    : parent.canImport(comp, flavor);
            }
            return result;
        }

        public int getSourceActions(JComponent comp){
            return COPY_OR_MOVE;
        }

        public Transferable createTransferable(JComponent c){
            return new StringSelection(textfield.getSelectedText());
        }

        public boolean importData(JComponent comp, Transferable t){
            boolean result = false;
            if(
                 t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
            ){
                List data = null;
                try{
                   data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (UnsupportedFlavorException ex){
                    throw new Error(ex);
                } catch (IOException ex){
                    logger.severe(ex.toString());
                }
                if(data != null && !data.isEmpty()){
                    textfield.setText(((File) data.get(0)).getAbsolutePath());
                    result = true;
                }
            } else {
                if(parent == null) {
                    result = super.importData(comp, t);
                } else {
                    result = parent.importData(comp, t);
                }
            }
            if(result){
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        fireActionPerformed();
                    }
                });
            }
            return result;
        }

    }
}