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

import java.awt.Component;
import javax.swing.JTabbedPane;

/** A {@link javax.swing.JTabbedPane JTabbedPane} where
 * CTRL+T activates the next tab and to which tabs
 * can be added conveniently.
 *
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 * TODO: Fix Ctrl-T
 */
public class JBook extends JTabbedPane{

    /** Constructs a new instance of this class. **/
    public JBook(){
        super();
    }

    /** Binds CTRL+T to "next tab". (NOT WORKING!!!)*/
    public void registerCtrlT(){
        // KeyStroke ks = KeyStroke.
            // getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_MASK);
        // getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks,"next");
        // getActionMap().put("next",new AbstractAction(){
                // public void actionPerformed(ActionEvent e){
                    // setSelectedIndex((getSelectedIndex()+1) % getTabCount());
                // }
            // });
    }

    /** Adds a tab named <code>name</code> and sets its mnemonic.
     * @see java.awt.event.KeyEvent
     */
    public void add(Component component,String name, Integer mnemonic){
        super.add(component,name);
        int i = indexOfComponent(component);
        setMnemonicAt(i,mnemonic.intValue());
        setToolTipTextAt(i," ");
    }
}
