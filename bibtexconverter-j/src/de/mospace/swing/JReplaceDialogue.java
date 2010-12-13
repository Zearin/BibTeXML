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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import de.mospace.swing.text.HistoryTextField;

/** A customizable Replace Dialogue.
 *
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 */
abstract public class JReplaceDialogue extends JDialog implements ActionListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5162323873902367105L;

	/** A common interface for JComboBoxes and JTextFields. **/
    private static interface TextField{
        public void setText(String s);
        public String getText();
        public JComponent getComponent();
    }

    /** Extracted from bsh.BSHLiteral by Pat Niemeyer */
    private static class StringLiteral{
        private final String value;

        public StringLiteral(String source){
            value = stringSetup(source);
        }

        private static String stringSetup(String str){
            StringBuffer buffer = new StringBuffer();
            for(int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                if(ch == '\\') {
                    // get next character
                    ch = str.charAt(++i);
                    if(Character.isDigit(ch)) {
                        int endPos = i;
                        // check the next two characters
                        while(endPos < i + 2)
                        {
                            if(Character.isDigit(str.charAt(endPos + 1)))
                                endPos++;
                            else
                                break;
                        }
                        ch = (char)Integer.parseInt(str.substring(i, endPos + 1), 8);
                        i = endPos;
                    } else {
                        ch = getEscapeChar(ch);
                    }
                }
                buffer.append(ch);
            }
            return buffer.toString();
        }

        private static char getEscapeChar(char ch)
        {
            switch(ch)
            {
                case 'b':
                    ch = '\b';
                    break;
                case 't':
                    ch = '\t';
                    break;
                case 'n':
                    ch = '\n';
                    break;
                case 'f':
                    ch = '\f';
                    break;
                case 'r':
                    ch = '\r';
                    break;
                // do nothing - ch already contains correct character
                case '"':
                case '\'':
                case '\\':
                break;
            }
            return ch;
        }

        public String toString(){
            return value;
        }
    }

    /** A static class that wraps JComboBoxes and JTextFields into TextFields.
    * @see #TextField
    **/
    private final static class TextFieldFactory {
        private TextFieldFactory(){
        }

        public static TextField getTextField(final JComboBox jcb){
            return new TextField(){
                private JComboBox cb=jcb;

                public void setText(String s){
                    jcb.setSelectedItem(s);
                }

                public String getText(){
                    return jcb.getSelectedItem().toString();
                }

                public JComponent getComponent(){
                    return jcb;
                }
            };
        }

        public static TextField getTextField(final JTextField tf){
         return new TextField(){
                public void setText(String s){
                    tf.setText(s);
                }

                public String getText(){
                    return tf.getText();
                }

                public JComponent getComponent(){
                    return tf;
                }
            };
        }
    }

    private JPanel cont;
    private TextField replWhat;
    private TextField replWith;
    /** The action event that has triggered the dialogue **/
    ActionEvent opener;
    private Object scope;

    private JCheckBox matchWord;
    private JCheckBox matchCase;
    private JCheckBox regExp;
    private JButton repl;
    private JButton replAll;
    private JButton cancel;
    private JLabel replWhatL;
    private JLabel replWithL;
    private JPanel north, nwest, neast;
    private JPanel south, swest, seast;
    private ActionListener replListener;

    final public static int MATCH_WORD = 0;
    final public static int MATCH_CASE = 1;
    final public static int REG_EXP = 2;

    /** Constructs a replace dialog with no history of entered items. **/
    public JReplaceDialogue(Frame owner){
        this(owner,null,0);
    }

    /** Constructs a replace dialog with a search and replace history.
     * @param pref the preference node to use for storing history items
     * @param len the number of items in the history
    **/
    public JReplaceDialogue(Frame owner, Preferences pref, int len){
        this(owner, pref, len, true);
    }

    /** Constructs a replace dialog with a search and replace history.
     * @param pref the preference node to use for storing history items
     * @param len the number of items in the history
     * @param showReplace whether to display the Replace All button and
     * the "[Replace ]with" field
    **/
    public JReplaceDialogue(Frame owner, Preferences pref, int len, boolean showReplace){
        super(owner);
        replListener = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                boolean zAll=false;
                if(e.getSource() == replAll){
                    zAll = true;
                }
                final String lookFor = replWhat.getText();
                if(lookFor == null || lookFor.length() == 0){
                    return;
                }
                String rWith = replWith.getText();
                final boolean rE = regExp.isSelected();
                if(rE){
                    rWith = (new StringLiteral(rWith)).toString();
                }
                performReplace(lookFor, rWith, matchWord.isSelected(),
                        matchCase.isSelected(), rE, zAll, scope);
            }
        };

        CompoundBorder border = BorderFactory.
                createCompoundBorder(BorderFactory.
                createEmptyBorder(5,5,5,5),BorderFactory.
                createEtchedBorder());

        cont = new JPanel();
        north = new JPanel();
        neast = new JPanel();
        nwest = new JPanel();
        south = new JPanel();
        swest = new JPanel();
        seast = new JPanel();

        AbstractAction cancelAction = new AbstractAction(UIManager.getString("OptionPane.cancelButtonText")){
                /**
			 * 
			 */
			private static final long serialVersionUID = 6595495368666347281L;

				public void actionPerformed(ActionEvent e){
                    cancel();
                }
            };

        AbstractAction findAction = new AbstractAction(
                GLOBALS.getString(showReplace? "Find/Replace..." : "Find...")){
            /**
					 * 
					 */
					private static final long serialVersionUID = 3952586083683443779L;

			public void actionPerformed(ActionEvent e){
                replListener.actionPerformed(
                    new ActionEvent(repl,ActionEvent.ACTION_PERFORMED,""));
            }
        };

        this.setTitle(GLOBALS.getString(showReplace?"Replace":"Find"));

        matchWord = new JCheckBox(GLOBALS.getString("whole word"));
        matchWord.setMnemonic(GLOBALS.getKey("W (whole word mnemonic)"));

        matchCase = new JCheckBox(GLOBALS.getString("case sensitve"));
        matchCase.setMnemonic(GLOBALS.getKey("S (case sensitive mnemonic)"));

        regExp = new JCheckBox(GLOBALS.getString("regular expression"));
        regExp.setMnemonic(GLOBALS.getKey("E (regular expression mnemonic)"));

        repl = new JButton(findAction);
        repl.setMnemonic(GLOBALS.getKey(showReplace?
            "F (Find/Replace... mnemonic)" : "F (Find... mnemonic)"));

        replAll = new JButton(GLOBALS.getString(
                showReplace? "Replace all" : "Select all"));
        replAll.setMnemonic(GLOBALS.getKey(showReplace?
                "A (replace all mnemonic)" : "A (select all mnemonic)"));

        cancel = new JButton(cancelAction);
        cancel.setMnemonic(GLOBALS.getKey("C (cancel mnemonic)"));

        replWhatL = new JLabel(GLOBALS.getString("Look for"));
        replWhatL.setDisplayedMnemonic(GLOBALS.getKey("L (Look for mnemonic)"));

        replWithL = new JLabel(
                showReplace ? GLOBALS.getString("Replace with") : "");
        replWithL.setDisplayedMnemonic(GLOBALS.getKey("I (with mnemonic)"));

        if(pref == null){
            JTextField jtf1 = new JTextField(72);
            replWhatL.setLabelFor(jtf1);
            replWhat    = TextFieldFactory.getTextField(jtf1);
            JTextField jtf2 = new JTextField(72);
            replWithL.setLabelFor(jtf2);
            replWith    = TextFieldFactory.getTextField(jtf2);
        } else {
            HistoryTextField htf = new HistoryTextField(pref, 0, len);
            htf.setAutoSave(true);
            JComboBox jcb1 = htf.getComboBox();
            htf = new HistoryTextField(pref, 1, len);
            htf.setAutoSave(true);
            JComboBox jcb2 = htf.getComboBox();
            replWhatL.setLabelFor(jcb1);
            replWithL.setLabelFor(jcb2);
            replWhat    = TextFieldFactory.getTextField(jcb1);
            replWith    = TextFieldFactory.getTextField(jcb2);
        }

        replAll.addActionListener(replListener);

        cont.setPreferredSize(new Dimension(400,130));
        north.setBorder(border);
        neast.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        nwest.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        swest.setBorder(BorderFactory.createEmptyBorder(0,40,5,40));
        seast.setBorder(BorderFactory.createEmptyBorder(0,60,5,60));
        replWith.getComponent().setPreferredSize(new Dimension(200,20));
        replWhat.getComponent().setPreferredSize(new Dimension(200,20));
        replWhatL.setHorizontalAlignment(SwingConstants.RIGHT);
        replWithL.setHorizontalAlignment(SwingConstants.RIGHT);
        final Dimension d = new Dimension(135,16);
        Insets in = new Insets(0,3,0,2);
        cancel.setPreferredSize(d);
        repl.setPreferredSize(d);
        replAll.setMargin(in);
        cancel.setMargin(in);
        repl.setMargin(in);
        replAll.setPreferredSize(d);
        matchWord.setPreferredSize(d);
        matchCase.setPreferredSize(d);
        regExp.setPreferredSize(d);

        cont.setLayout(new BorderLayout());
        north.setLayout(new BorderLayout());
        neast.setLayout(new GridLayout(0,1));
        nwest.setLayout(new GridLayout(0,1));
        south.setLayout(new GridLayout(1,0));
        swest.setLayout(new GridLayout(0,1,0,3));
        seast.setLayout(new GridLayout(0,1,0,3));

        nwest.add(replWhatL);
        neast.add(replWhat.getComponent());
        if (showReplace) {
            nwest.add(replWithL);
            neast.add(replWith.getComponent());
        }
        north.add(nwest, BorderLayout.WEST);
        north.add(neast, BorderLayout.CENTER);

        swest.add(matchWord);
        swest.add(matchCase);
        swest.add(regExp);
        seast.add(repl);
        seast.add(replAll);
        seast.add(cancel);
        south.add(swest);
        south.add(seast);

        cont.add(north, BorderLayout.CENTER);
        cont.add(south, BorderLayout.SOUTH);
        this.setContentPane(cont);
        this.pack();

        /* Conflicts with history text fields */
        //KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
        //setKeyToAction(cont,esc,cancelAction);
        //KeyStroke ret = KeyStroke.getKeyStroke("ENTER");
        //setKeyToAction(neast,ret,findAction);
    }

    // /** Binds a key to an action in a component and all of its children.
     // */
    // private void setKeyToAction(Component component, KeyStroke k, Action a){
        // if(component instanceof JComponent){
            // JComponent c = (JComponent) component;
            // c.getInputMap().put(k,"cancel");
            // c.getActionMap().put("cancel",a);
            // if(component instanceof Container){
                // Container container = (Container) component;
                // Component[] components = container.getComponents();
                // for (int i=0; i< components.length; i++){
                    // setKeyToAction(components[i],k,a);
                // }
            // }
        // }
    // }

    /** Specifies the scope of the next search.
     * @see #performReplace(String what, String with, boolean mWord,
     * boolean mCase, boolean regExp, boolean rAll, Object scope)
     */
    public void setScope(Object searchScope){
        scope = searchScope;
    }

    /** Displays the dialogue.
    *  @deprecated
    *  @see #setVisible
    */
    public void show(){
    //Don't call setVisible here!
    //JDialog implements setVisible(true) by calling show()
    //therefore a call to setVisible would lead to an infinite loop
        super.show();
        replWhat.getComponent().requestFocus();
    }

    /** Displays or hides the dialogue. */
    public void setVisible(boolean b){
        super.setVisible(b);
        if(b) {
            replWhat.getComponent().requestFocus();
        }
    }

    /** Closes the dialog without doing a search. This method is called when
     * the user hits the cancel button. **/
    public void cancel(){
        setVisible(false);
    }

    /** Hides or shows an option. Use this if you don't want to implement
     * one of the options.
     * @param option one of MATCH_WORD, MATCH_CASE, REG_EXP
     */
    public void setOptionEnabled(int option, boolean enabled){
        switch (option){
            case MATCH_WORD:matchWord.setVisible(enabled);break;
            case MATCH_CASE:matchCase.setVisible(enabled);break;
            case REG_EXP:regExp.setVisible(enabled);break;
            default: throw new IllegalArgumentException("Unknown option " + option);
        }
    }

    /** Perform a find/replace operation.
     * Overwrite this method to suit your needs.
     * @param what string or regular expression to look for
     * @param with replacement string
     * @param mWord modifier: match whole word
     * @param mCase modifier: match case
     * @param regExp modifier: <code>what</code> is a regular expression
     * @param rAll modifier: replace all occurences in scope
     * @param scope search scope set by {@link #setScope(Object scope)}
     */
    abstract public void performReplace(String what, String with, boolean mWord,
            boolean mCase, boolean regExp, boolean rAll, Object scope);

    /** Show the dialogue when an action is performed */
    public void actionPerformed(ActionEvent e){
        opener = e;
        setVisible(true);
    }
}
