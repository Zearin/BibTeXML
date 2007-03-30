/* $Id: MiniShell.java,v 1.11 2006/10/12 11:41:03 ringler Exp $
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

/** This class associates a ProcessIOPane with an instance of JFrame.
 * The ProcessIOPane can either be docked in
 * the upper part of the JFrame or float in a separate window owned by the
 * JFrame. It can be switched between the two states by means of 'float' and
 * 'dock' buttons. Additionally buttons are provided for the control actions of
 * the ProcessIOPane.
 *
 *  @version $Revision: 1.11 $ ($Date: 2006/10/12 11:41:03 $)
 *  @author Moritz Ringler
 **/
public class MiniShell extends JPanel {

    private JDialog dialg;
    private Action dockAction = new AbstractAction(GLOBALS.getString("Dock")) {
        public void actionPerformed(ActionEvent e) {
            dock();
        }
    };

    private JButton dockButton;
    private Action floatAction = new AbstractAction(GLOBALS.getString("Float")) {
        public void actionPerformed(ActionEvent e) {
            undock();
        }
    };

    private JButton floatButton;
    private boolean floating= false;
    private JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JToolBar jtb = new JToolBar();
    private String name;
    private JFrame parent;
    private ProcessIOPane piop;
    /* must not fail when the ActionEvent is null */
    private Action showAction = new AbstractAction(GLOBALS.getString("Console")){
        public void actionPerformed(ActionEvent e) {
            if (isFloating()) {
                showDialog();
            } else {
                showDocked();
            }
        }
    };

    private Action closeAction = new AbstractAction(GLOBALS.getString("Close")){
        public void actionPerformed(ActionEvent e) {
            close();
        }
    };

    private boolean close(){
        if (isFloating()) {
            dialg.setVisible(false);
        } else {
            hideDocked();
        }
        return true;
    }

    /**
     * Creates a new MiniShell object.
     *
     * @param f the JFrame this MiniShell depends on, must be non-null
     * @param w the preferred width of this component
     * @param h the preferred height of this component
     * @param title the title of this MiniShell when floating
     */
    public MiniShell(JFrame f, int w, int h, String title) {
        this(f, w, h, title, false);
    }

    /**
     * Creates a new MiniShell object.
     *
     * @param f the JFrame this MiniShell depends on, must be non-null
     * @param w the preferred width of this component
     * @param h the preferred height of this component
     * @param title the title of this MiniShell when floating
     */
    public MiniShell(JFrame f, int w, int h, String title, boolean floating) {
        init(f, w, h, title, new ProcessIOPane(5, 50){
            protected boolean builtin(String s){
                return  super.builtin(s) || (s.equals("exit") && close());
            }

            protected Preferences getPref(){
                return MiniShell.this.getPref().node("ProcessIOPane");
            }
        }, floating);
    }

    /**
     * Creates a new MiniShell object from the specified ProcessIOPane.
     *
     * @param f the JFrame this MiniShell depends on, must be non-null
     * @param w the preferred width of this component
     * @param h the preferred height of this component
     * @param title the title of this MiniShell when floating
     * @param pip the ProcessIOPane to wrap
     */
    public MiniShell(JFrame f, int w, int h, String title, ProcessIOPane pip) {
        init(f,w,h,title,pip, false);
    }

    private void init(JFrame f, int w, int h, String title, ProcessIOPane pip, boolean floating){
        this.floating= floating;
        piop = pip;
        jtb.setRollover(true);
        setLayout(new BorderLayout());
        add(piop, BorderLayout.CENTER);
        add(jtb, BorderLayout.PAGE_START);
        Action ashell = piop.getSystemShellAction();
        JButton jb = addButton(ashell);
        jb.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                if(e.isPopupTrigger()){ //getButton() != MouseEvent.BUTTON1){
                    piop.setSystemShell(piop.querySystemShell());
                }
            }
            public void mouseReleased(MouseEvent e){
                if(e.isPopupTrigger()){ //getButton() != MouseEvent.BUTTON1){
                    piop.setSystemShell(piop.querySystemShell());
                }
            }
        });
        Action[] piopa = piop.getControlActions();
        for (int i = 0; i < piopa.length; i++) {
            addButton(piopa[i]);
        }
        parent = f;
        addButton(closeAction);
        floatButton = addButton(floatAction);
        dockButton = addButton(dockAction);
        dockButton.setVisible(floating);
        floatButton.setVisible(!floating);
        setPreferredSize(new Dimension(w, h));
        jsp.setResizeWeight(0.2);
        name = title;
    }

    protected Preferences getPref(){
        return Preferences.userNodeForPackage(MiniShell.class);
    }

    /**
     * Returns the action that hides this MiniShell.
     *
     * @return the action that hides this MiniShell
     */
    public Action getCloseAction() {
        return closeAction;
    }

    /**
     * Returns whether this MiniShell is currently floating.
     *
     * @return <code>true</code> when floating, <code>false</code> when docked
     */
    public boolean isFloating() {
        return floating;//(dialg != null && dialg.getContentPane().isAncestorOf(this));
    }

    /**
     * Returns the ProcessIOPane wrapped by this MiniShell.
     *
     * @return the ProcessIOPane wrapped by this MiniShell
     */
    public ProcessIOPane getProcessIOPane() {
        return piop;
    }

    /**
     * Returns the action that makes this MiniShell visible.
     *
     * @return the action that makes this MiniShell visible
     */
    public Action getShowAction() {
        return showAction;
    }

    /**
     * Adds a new button to this MiniShell.
     *
     * @param a the action to configure the button from
     * @return the JButton that has been created and added
     */
    private JButton addButton(Action a) {

        JButton jb = new JButton(a);

        //jb.setBorderPainted(false);
        jb.setOpaque(false);
        jb.setBackground(jtb.getBackground());
        jtb.add(jb);
        return jb;
    }

    /**
     * Docks this MiniShell into its JFrame.
     */
    private void dock() {
        if (!isFloating()) {
            return;
        }
        hideDialog();
        showDocked();
    }

    /**
     * Hides this MiniShell when floating.
     */
    private void hideDialog() {
        ((JComponent) dialg.getContentPane()).setPreferredSize(dialg.getSize());
        dialg.getContentPane().remove(this);
        dialg.setVisible(false);
    }

    /**
     * Hides this MiniShell when docked.
     */
    private void hideDocked() {

        Dimension parentsize = parent.getContentPane().getSize();
        parent.setContentPane((Container) jsp.getBottomComponent());
        ((JComponent) parent.getContentPane()).setPreferredSize(parentsize);
        jsp.remove(this);
        jsp.remove(parent.getContentPane());
        parent.pack();
    }

    /**
     * Shows this MiniShell floating.
     */
    private void showDialog() {
        floating = true;
        if(dialg == null || ! dialg.isVisible()){
            Dimension parentsize = parent.getContentPane().getSize();
            if (dialg == null) {
                dialg = new JDialog(parent, name);
            }
            dockButton.setVisible(true);
            floatButton.setVisible(false);
            dialg.getContentPane().add(this);
            ((JComponent) parent.getContentPane()).setPreferredSize(parentsize);
            dialg.pack();
            dialg.setVisible(true);
            piop.requestFocus();
        }
    }

    /**
     * Shows this MiniShell docked.
     */
    private void showDocked() {
        floating = false;
        if(jsp.getTopComponent() != this){
        Dimension parentsize = parent.getContentPane().getSize();
        dockButton.setVisible(false);
        floatButton.setVisible(true);

        //Container c = parent.getContentPane();
        //c.setPreferredSize(c.getSize());
        jsp.setTopComponent(this);
        jsp.setBottomComponent(parent.getContentPane());
        parent.setContentPane(jsp);
        ((JComponent) parent.getContentPane()).setPreferredSize(parentsize);
        jsp.resetToPreferredSizes();
        parent.pack();
        piop.requestFocus();
        }
    }

    /**
     * Makes this MiniShell float.
     */
    private void undock() {
        if (isFloating()) {
            return;
        }
        hideDocked();
        showDialog();
    }
}