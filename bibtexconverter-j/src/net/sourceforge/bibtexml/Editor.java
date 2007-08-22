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
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.xml.sax.*;
import net.sourceforge.jeditsyntax.*;

/** This minimal editor allows to correct errors in BibTeX or XML input files. */
public class Editor{
    private final JEditTextArea area = new JEditTextArea();
    private XFile activeFile = null;
    private final JDialog dialog;
    private final TokenMarker xmlTokenMarker = new XMLTokenMarker();
    private final TokenMarker bibTokenMarker = new BibTeXTokenMarker();
    private final Container toolbar = Box.createHorizontalBox();
    protected boolean dirty = false;
    private long lastModified = 0l;

    private final DocumentListener dirtyMarker = new DocumentListener(){
        public void insertUpdate(final DocumentEvent e){
            markDirty(true);
        }

        public void removeUpdate(final DocumentEvent e){
            markDirty(true);
        }

        public void changedUpdate(final DocumentEvent e){
            markDirty(true);
        }
    };

    private final Action saveAction = new AbstractAction("Save"){
        public void actionPerformed(final ActionEvent e){
            save();
        }
    };

    public Editor(Window parent){
        if(parent instanceof Frame){
            dialog = new JDialog((Frame) parent);
        } else if (parent instanceof Dialog){
            dialog = new JDialog((Dialog) parent);
        } else {
            dialog = null;
            throw new IllegalArgumentException("Unknown window type, must be adialog or frame.");
        }
        setupTextArea();
        toolbar.add(Box.createHorizontalStrut(3));
        toolbar.add(new JButton(saveAction));
        final JPanel cp = new JPanel(new BorderLayout());
        cp.add(toolbar, BorderLayout.NORTH);
        cp.add(area, BorderLayout.CENTER);
        dialog.setContentPane(cp);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.pack();
        markDirty(false);
    }

    private void setupTextArea(){
        area.getDocument().addDocumentListener(dirtyMarker);
        InputHandler inputHandler = area.getInputHandler();
        System.out.println(inputHandler.getClass());
        final ActionListener copy = new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    area.copy();
                }
        };
        final ActionListener paste = new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    area.paste();
                }
        };
        final ActionListener cut = new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    area.cut();
                }
        };
        inputHandler.addKeyBinding("COPY", copy);
        inputHandler.addKeyBinding("C+C", copy);
        inputHandler.addKeyBinding("C+INSERT", copy);
        inputHandler.addKeyBinding("PASTE", paste);
        inputHandler.addKeyBinding("C+V", paste);
        inputHandler.addKeyBinding("S+INSERT", paste);
        inputHandler.addKeyBinding("CUT", cut);
        inputHandler.addKeyBinding("C+X", cut);
        inputHandler.addKeyBinding("S+DELETE", cut);
    }

    private String activeFilePath(){
        String result = null;
        if(activeFile == null){
            result = "No file loaded.";
        } else {
            try{
                result = activeFile.getCanonicalPath();
            } catch (IOException ex){
                result = activeFile.getAbsolutePath();
            }
        }
        return result;
    }

    private boolean isModified(){
        return (activeFile != null) &&
            (lastModified != activeFile.lastModified());
    }

    private void markDirty(final boolean b){
        if(dirty == b){
            return;
        }
        dirty = b;
        if(dirty){
            dialog.setTitle(activeFilePath() + " (modified) ");
        } else {
            dialog.setTitle(activeFilePath());
        }
        saveAction.setEnabled(dirty);
    }

    public boolean showFile(final XFile file){
        boolean load = false;
        boolean success = true;
        if(file == null){
            if(!dirty || askSaveDirtyBuffer()){
                area.setText("");
                dialog.setTitle("");
            }
            load = false;
        } else if (file.equals(activeFile)){
            if(activeFile.exists()){
                load = isModified() && (!dirty || askReloadModifiedFile());
            } else {
                load = false;
                lastModified = 0l;
                markDirty(true);
            }
        } else {
            load = (dirty)? askSaveDirtyBuffer() : true;
        }
        if (load){
            activeFile = null;
            area.setText("");
            switch(file.getType()){
                case BIBXML:
                    area.setTokenMarker(xmlTokenMarker);
                    break;
                case BIBTEX:
                    area.setTokenMarker(bibTokenMarker);
                    break;
                default:
                    throw new Error("Unknown input type " + file.getType());
            }
            success = loadFile(file);
        }
        return success;
    }

    private boolean askReloadModifiedFile(){
        final int result = JOptionPane.showConfirmDialog(dialog,
            "<html>The file that you are editing has been modified outside the editor.<br>" +
            "Do you want to reload the file from disk" +
            ((dirty)?" and discard your changes?" : "?") + "</html>",
            "Reload modified file?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        return (result == JOptionPane.YES_OPTION);
    }

    private boolean askSaveDirtyBuffer(){
        final int result = JOptionPane.showConfirmDialog(dialog,
            "<html>You have not saved your changes to " + activeFile.getName() +
            ".<br> Do you want to do so now before closing it?",
            "Save changes?",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        boolean returnVal = false;
        switch(result){
            case JOptionPane.YES_OPTION:
                returnVal = save();
                break;
            case JOptionPane.NO_OPTION:
                returnVal = true;
                break;
            default:
                returnVal = false;
        }
        return returnVal;
    }

    private synchronized boolean loadFile(final XFile f){
        InputStreamReader reader = null;
        boolean success = true;
        try{
            StringBuilder sb = new StringBuilder();
            reader = new InputStreamReader(
                    new BufferedInputStream(new FileInputStream(f)),
                    f.getCharset());
            final char[] c = new char[1024];
            int read = 0;
            while((read = reader.read(c, 0, 1024)) != -1){
                sb.append(c, 0, read);
            }
            area.setText(sb.toString());
            sb = null;
            activeFile = f;
            lastModified = f.lastModified();
            dirty = true; //otherwise markDirty won't do anything
            markDirty(false);
            success = true;
        } catch (IOException ex){
            JOptionPane.showMessageDialog(
                dialog,
                "<html>Cannot load file " + f.getAbsolutePath() + "<br>" +
                ex.getMessage(),
                "Error loading file",
                JOptionPane.WARNING_MESSAGE
            );
            success = false;
        } finally {
            if(reader != null){
                try{
                    reader.close();
                } catch (Exception ex){
                    System.err.println(ex);
                    System.err.flush();
                }
            }
        }
        if(success){
            try{
                dialog.setTitle(f.getCanonicalPath());
            } catch (IOException ex){
                dialog.setTitle(f.getAbsolutePath());
            }
        }
        return success;
    }

    protected synchronized boolean save(){
        if(activeFile == null){
            return true;
        }
        boolean success = true;
        OutputStreamWriter writer = null;
        try{
            writer = new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(activeFile)),
                    activeFile.getCharset());
            final String text = area.getText();
            writer.write(text, 0, text.length());
            writer.flush();
            markDirty(false);
            lastModified = activeFile.lastModified();
            success = true;
        } catch (IOException ex){
            JOptionPane.showMessageDialog(
                dialog,
                "<html>Cannot save file " + activeFilePath() + "<br>" +
                ex.getMessage(),
                "Error saving file",
                JOptionPane.WARNING_MESSAGE
            );
            success = false;
        } finally {
            if(writer != null){
                try{
                    writer.close();
                } catch (Exception ex){
                    System.err.println(ex);
                    System.err.flush();
                }
            }
        }
        return success;
    }

    public void goTo(final int line, final int column){
        int offset = area.getLineStartOffset(line - 1);
        offset += column;
        area.setCaretPosition(offset - 1);
        area.scrollToCaret();
    }

    public Window window(){
        return dialog;
    }
}