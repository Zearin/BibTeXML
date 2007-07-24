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
package de.mospace.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;

public class ListPicker extends JDialog{
    private ListModel model;
    private final JList list = new JList();
    private Object value;
    private final Container customButtons = new JPanel();

    public ListPicker(Frame f, Object[] obj){
        super(f);
        model = new DefaultComboBoxModel(obj);
        init(f);
    }

    public ListPicker(Dialog d, Object[] obj){
        super(d);
        model = new DefaultComboBoxModel(obj);
        init(null);
    }

    public ListPicker(Frame f, Collection c){
        super(f);
        model = makeModel(c);
        init(f);
    }

    public ListPicker(Dialog d, Collection c){
        super(d);
        model = makeModel(c);
        init(null);
    }

    private ListModel makeModel(Collection c){
        Vector v = null;
        if(c instanceof Vector){
            v = (Vector) c;
        } else {
            v = new Vector(c.size());
            v.addAll(c);
        }
        return new DefaultComboBoxModel(v);
    }

    private void init(Frame f){
        setModal(true);
        JPanel panel = new JPanel(new BorderLayout());

        list.setModel(model);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        Action okAction = new AbstractAction(UIManager.getString("OptionPane.okButtonText")){
            public void actionPerformed(ActionEvent e){
                value = list.getSelectedValue();
                if(value != null){
                    ListPicker.this.setVisible(false);
                }
            }
        };
        list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"commit");
        list.getActionMap().put("commit", okAction);


        JScrollPane sp = new JScrollPane(list);
        panel.add(sp, BorderLayout.CENTER);

        JButton buttonOK     = new JButton(okAction);
        JButton buttonCancel = new JButton();
        buttonCancel.setText(UIManager.getString("OptionPane.cancelButtonText"));
        buttonOK.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    //do nothing
                }
        });
        buttonCancel.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    value = null;
                    ListPicker.this.setVisible(false);
                }
        });
        JPanel mainButtons = new JPanel();
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(mainButtons, BorderLayout.CENTER);
        buttons.add(customButtons, BorderLayout.SOUTH);
        mainButtons.add(buttonCancel);
        mainButtons.add(buttonOK);
        panel.add(buttons, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(500,500));
        setContentPane(panel);
        pack();
        Dimension dlgSize = getPreferredSize();
        if(f != null){
            Dimension frmSize = f.getSize();
            Point loc = f.getLocation();
            setLocation((frmSize.width - dlgSize.width) / 2 +
                loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        if(model.getSize() > 0){
            list.setSelectedIndex(0);
        }
    }

    public void addButton(Component c){
        customButtons.add(c);
    }

    public Object getValue(){
        return value;
    }


    public void setValue(Object o){
        value = o;
    }

    private static class PickerButton extends JButton{
        final String bvalue;
        final ListPicker picker;

        public PickerButton(String title, String pvalue, ListPicker picker){
            super(title);
            this.bvalue = pvalue;
            this.picker = picker;
            addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        PickerButton.this.picker.setValue(bvalue);
                        PickerButton.this.picker.setVisible(false);
                    }
            });
        }
    }

    public void addPickerButton(String title, String pvalue){
        JButton pb = new PickerButton(title, pvalue, this);
        addButton(pb);
    }

    public void setVisible(boolean b){
        super.setVisible(b);
        if(b){
            list.requestFocusInWindow();
        }
    }

    public void setCellRenderer(ListCellRenderer renderer){
        list.setCellRenderer(renderer);
    }

    private void setModel(){
        list.setModel(model);
        if(model.getSize() > 0){
            list.setSelectedIndex(0);
        }
    }

    public void setChoice(Object[] obj){
        model = new DefaultComboBoxModel(obj);
        setModel();
    }

    public void setChoice(Collection c){
        model = makeModel(c);
        setModel();
    }
}
