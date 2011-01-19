package de.mospace.swing.text;

/* de.mospace.swing library
* Copyright (C) 2005 Moritz Ringler
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/** A text field that remembers the last values entered. The GUI representation
* is actually a JComboBox that can be retrieved with the {@link #getComboBox
* getJComboBox} method.
* @author Moritz Ringler
* @version $Revision$ ($Date$)
**/
public class HistoryTextField {
    private final static int MINIMUM_LENGTH = 5;
    private JComboBox jcb;
    private int length = MINIMUM_LENGTH;
    private final Preferences loadedFrom;
    private final int loadId;
    private ActionListener autoSaver = null;
    private boolean autosave = false;

    /** Constructs a HistoryTextField with the specified id
    * and initializes it with at most <code>len</code>
    * previously saved values.
    * @param prefNode the preference node that values will be loaded from
    * @param id an integer identifying the history text field whose values
    * to load
    * @param len the maximum number of values to load from the preferences,
    * if it is {0 | negative} then {all | no} saved values will be loaded
    */
    public HistoryTextField(Preferences prefNode, int id, int len) {
        loadedFrom = prefNode;
        loadId = id;
        load(prefNode, id, len);
        jcb.setEditable(true);
    }

    /** Constructs a HistoryTextField with the specified id
    * and initializes it with all previously saved values.
    * @param prefNode the preference node that values will be loaded from
    * @param id an integer identifying this history text field whose values
    * to load
    */
    public HistoryTextField(Preferences prefNode, int id)
    {
        this(prefNode, id, 0);
    }


    private void load(Preferences prefNode, int id, int llen){
        String hist = prefNode.get("HistoryTextField"+id,"\n\n\n\n");
        String[] loadedItems = hist.split("\n",-1);
        int len = llen;
        int hlen = loadedItems.length;
        if (len == 0){
            len = hlen;
        }
        if (len < MINIMUM_LENGTH) {
            len = MINIMUM_LENGTH;
        }
        DefaultComboBoxModel mod = new DefaultComboBoxModel(new String[len]);
        int i=0;
        int xlen = Math.min(len,hlen);
        for(; i<xlen; i++){
            mod.insertElementAt(loadedItems[i],i);
        }
        for(; i<len;i++){
            mod.insertElementAt(" ",i);
        }
        jcb = new JComboBox(mod);
        jcb.setEditable(true);
        jcb.setSelectedItem(loadedItems[0]);
    }

    /** Saves this text fields history to the specified preference node
    * and id.
    * @param prefNode the preference node that values will be written to
    * @param id an integer identifying this history text field
    */
    public void save(Preferences prefNode, int id){
        StringBuffer hist = new StringBuffer(length*20);
        for(int i=0; i<length-1;i++){
            hist.append(jcb.getItemAt(i).toString()).append("\n");
        }
        if (length>0){
            hist.append(jcb.getItemAt(length-1));
        }
        prefNode.put("HistoryTextField"+id,hist.toString());
    }

    /** Adds a new string to this text field's history.
    * @param str the string to add
    */
    public void addToHistory(String str){
        ((DefaultComboBoxModel) jcb.getModel()).insertElementAt(str,0);
    }

    /** Adds the item that is currently edited to this text field's history.
    */
    public void addCurrentItem(){
        addToHistory(jcb.getSelectedItem().toString());
    }

    /** Clears this text field's history. */
    public void clearHistory(){
        ((DefaultComboBoxModel) jcb.getModel()).removeAllElements();
    }

    /** Gets the single JComboBox object that serves as a GUI representation of
    * this history text field.
    * @return the combo box for this history text field
    * @see #setAutoSave
    */
    public JComboBox getComboBox(){
        return jcb;
    }

    /** Sets whether items newly added to this text field's combo box will
    * automatically be added to the history and saved under the preference node
    * and id that this text field has been loaded from.
    * @param b whether new items should be saved automatically
    * @see #isAutoSave
    */
    public synchronized void setAutoSave(boolean b){
        if(b && !autosave){
            if(autoSaver == null){
                autoSaver = new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        JComboBox cb = (JComboBox) e.getSource();

                        /* only add new items to the history */
                        if (cb.getSelectedIndex() != -1){
                            return;
                        }
                        Object item = cb.getSelectedItem();
                        /* only add non-null items */
                        if(item == null){
                            return;
                        }
                        addToHistory(item.toString());
                        save(loadedFrom,loadId);
                    }
                };
            }
            jcb.addActionListener(autoSaver);
            autosave = true;
        } else if (!b && autosave){
            jcb.removeActionListener(autoSaver);
            autosave = false;
        }
    }

    /** Returns whether items newly added to this text field's combo box will
    * automatically be added to the history and saved under the preference node
    * and id that this text field has been loaded from.
    * @return whether new items are saved automatically
    * @see #setAutoSave
    */
    public synchronized boolean isAutoSave(){
        return autosave;
    }

    /** Returns the items in this text field's history as a newly allocated
    * String array.
    * @return this text field's history
    */
    public String[] getStringArray(){
        String[] history = new String[length];
        for(int i=0; i<length; i++){
            history[i] = jcb.getItemAt(i).toString();
        }
        return history;
    }
}
