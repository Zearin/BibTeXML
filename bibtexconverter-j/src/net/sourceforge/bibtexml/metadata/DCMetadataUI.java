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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.JTextComponent;


/** A DCMetadata view. By default fires no action events. Subclasses may change this
behaviour by adding themselves as ActionListeners to the editor components. **/
public class DCMetadataUI extends JPanel implements ActionListener {
    private final static int TEXTFIELD_SIZE = 20;
    private static final List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
    private List<ActionListener> listenerList;
    private Map<String, JComponent> editors = new HashMap<String, JComponent>();

    /** @throws NullPointerException if field is <code>null</code>.
        @throws ClassCastException if value is of incorrect type.
    **/
    public void setValue(String field, Object value){
        String fieldlc = field.toLowerCase();
        JComponent comp = getComponent(fieldlc);
        if(comp instanceof JTextComponent){
            ((JTextComponent) comp).setText((String) value);
        } else if (comp instanceof JComboBox){
            if("language".equals(fieldlc)){
                if(locales.indexOf(value) >= 0){
                    ((JComboBox) comp).setSelectedItem(((Locale) value).getDisplayName());
                }
            }
        }
    }

    /** @return null if there is no editor for the specified field
    * the current value of the editor otherwise **/
    public Object getValue(String field){
        JComponent comp = getComponent(field.toLowerCase());
        if(comp instanceof JTextComponent){
            return ((JTextComponent) comp).getText();
        } else if (comp instanceof JComboBox){
            JComboBox box = (JComboBox) comp;
            if("language".equals(box.getActionCommand())){
                for(Locale loc : locales){
                    final String value = (String) box.getSelectedItem();
                    if(value == null || "".equals(value)){
                        return null;
                    } else if(loc.getDisplayName().equals(value)){
                        return loc;
                    }
                }
            }
        }
        return null;
    }

    protected JComponent getComponent(String field){
        return editors.get(field);
    }

    public DCMetadataUI(){
        initUI();
        //default constructor
    }

    private void initUI(){
        setLayout(new SpringLayout());
        int rowcount = 0;
        initTextField("Creator");
        rowcount ++;
        initTextField("Title");
        rowcount ++;
        initTextField("Subject");
        rowcount ++;
        initTextArea("Description", 5);
        rowcount ++;
        initTextField("Publisher");
        rowcount ++;
        initTextField("Contributor");
        rowcount ++;
        //initTextField("Identifier");
        //rowcount ++;
        initTextField("Source");
        rowcount ++;
        initLocaleSelector("Language");
        rowcount ++;
        initTextField("Relation");
        rowcount ++;
        initTextField("Coverage");
        rowcount ++;
        initTextField("Rights");
        rowcount ++;
        de.mospace.swing.SpringUtilities.makeCompactGrid(this, rowcount, 2, 0, 0, 3, 3);
    }

    private void initComponent(String label, JComponent comp){
        JLabel jlabel = new JLabel(label);
        editors.put(label.toLowerCase(), comp);
        jlabel.setLabelFor(comp);
        add(jlabel);
        add(comp);
    }

    private void initTextArea(String label, int rows){
        JTextArea textfield = new JTextArea(rows, TEXTFIELD_SIZE);
        textfield.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(textfield);
        initComponent(label, jsp);
        editors.put(label.toLowerCase(), textfield);
    }

    private void initLocaleSelector(String label){
        final int n = locales.size() + 1;
        final String[] combolist = new String[n];
        combolist[0] = "";
        Iterator<Locale> it = locales.iterator();
        for(int i=1; i<n; i++){
            combolist[i] = it.next().getDisplayName();
        }
        Arrays.sort(combolist);
        JComboBox selector = new JComboBox(combolist);
        selector.setActionCommand(label.toLowerCase());
        initComponent(label, selector);
    }

    private void initTextField(String label){
        JTextField textfield = new JTextField(TEXTFIELD_SIZE);
        textfield.setActionCommand(label);
        textfield.setToolTipText("Separate multiple entries by semicolon ;");
        initComponent(label, textfield);
    }

    protected synchronized void fireActionEvent(ActionEvent e){
        if(listenerList != null){
            for(ActionListener listener : listenerList){
                listener.actionPerformed(e);
            }
        }
    }

    public synchronized void addActionListener(ActionListener listener){
        if(listenerList == null){
             listenerList = new ArrayList<ActionListener>();
        }
        listenerList.add(listener);
    }

    public synchronized void removeActionListener(ActionListener listener){
        if(listenerList != null){
            listenerList.remove(listener);
        }
    }

    public void actionPerformed(ActionEvent e){
        fireActionEvent(e);
    }
}
