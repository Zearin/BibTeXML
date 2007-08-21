package net.sourceforge.bibtexml.metadata;
/*
 * $Id$
 *
 * Copyright (c) 2006 Moritz Ringler
 * This class is derived from EntryRetriever by Oskar Ojala
 * (also in this package)
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;


/** This class is not synchronized. **/
public class DCMetadataDialog extends JDialog {
    private transient boolean okPressed = false;
    private final DCMetadataController controller;
    private final ActionListener buttonListener = new ActionListener(){
        public void actionPerformed(ActionEvent e){
            if("OK".equals(e.getActionCommand())){
                okPressed = true;
                controller.updateModel();
            }
            setVisible(false);
        }
    };

    public boolean getOkPressed(){
        return okPressed;
    }

    public DCMetadataDialog(DCMetadata meta, Frame owner, String title){
        super(owner, title);
        controller = new DCMetadataController();
        controller.setModel(meta);
        final DCMetadataUI view = new DCMetadataUI();
        controller.setView(view);
        JPanel cp = new JPanel(new BorderLayout());
        //cp.add(new JLabel("Separate multiple entries by semicolon ;"), BorderLayout.NORTH);
        cp.add(view, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton b = new JButton(UIManager.getString("OptionPane.cancelButtonText"));
        b.addActionListener(buttonListener);
        b.setActionCommand("Cancel");
        buttons.add(b);
        b = new JButton(UIManager.getString("OptionPane.okButtonText"));
        b.addActionListener(buttonListener);
        b.setActionCommand("OK");
        buttons.add(b);
        cp.add(buttons, BorderLayout.SOUTH);
        setContentPane(cp);
    }

    public void setMetadata(DCMetadata model){
        controller.setModel(model);
        if(isVisible()){
            controller.updateView();
        }
    }

    public DCMetadata getMetadata(){
        return controller.getModel();
    }

    @Override
    public void setVisible(boolean b){
        if(b){
            okPressed = false;
            controller.updateView();
        }
        super.setVisible(b);
    }

}
