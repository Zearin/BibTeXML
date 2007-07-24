/* $Id: IOPane.java,v 1.9 2006/07/11 17:00:03 ringler Exp $
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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/** An IOPane consists of an input text field and a scrollable output text area.
 * The input text field is equipped with a history, i. e. you can access
 * previously entered items using the cursor keys. The output text area is
 * automatically scrolled to the bottom when new text is appended. By default
 * text that is entered in the input field is echoed in the output area and no
 * other action is taken.
 *
 * @version $Revision: 1.9 $ ($Date: 2006/07/11 17:00:03 $)
 * @author Moritz Ringler
 * @see ProcessIOPane
**/
public class IOPane extends JPanel {
    /** whether to prepend a prompt sign to the echoed input **/
    protected boolean prependPrompt = true;
    /** holds history items as strings **/
    private final transient History history = new VectorHistory();
    /** the input field */
    private final JTextField input = new JTextField();
    private final JScrollPane jsp = new JScrollPane(
                                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    /** the output area **/
    private final JTextArea jta;

    /** maximum number of lines in the output area */
    private int maxLines = 100;
    /** the JLabel preceding the input field */
    private JLabel prompt;
    /** The action listener that echoes input in the output field */
    protected final ActionListener echoAL = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (prependPrompt) {
                jta.append(prompt.getText());
            }
            jta.append(e.getActionCommand());
            jta.append("\n");
            if (e.getSource() == input) {
                input.setText("");
            }
        }
    };

    /**
     * Creates a new IOPane with no initial text.
     *
     * @param rows number of text rows >= 0
     * @param cols number of text columns >= 0 in a row
     * @throws IllegalArgumentException if the rows or columns arguments are negative.
     */
    public IOPane(int rows, int cols) {
        jsp.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {
            int max = 0;
            public void stateChanged(ChangeEvent e){
                BoundedRangeModel brm = ((BoundedRangeModel) e.getSource());
                if(!brm.getValueIsAdjusting() && brm.getMaximum() != max){
                    max = brm.getMaximum();
                    brm.setValue(max);
                }
            }
            ;});
        jta = new JTextArea(rows, cols);
        jta.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                trim();
            }

            public void removeUpdate(DocumentEvent e) {
                //does nothing
            }

            public void changedUpdate(DocumentEvent e) {
                trim();
            }
            ;});
        buildGui();
        input.addActionListener(echoAL);
        input.addActionListener(history.getAddListener());
        input.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {

                    String s = history.next();
                    if (s != null) {
                        input.setText(s);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {

                    String s = history.previous();
                    if (s != null) {
                        input.setText(s);
                    }
                }
            }
        });
        addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
               //does nothing
            }
            ;public void focusGained(FocusEvent e) {
                input.requestFocusInWindow();
            }
        });
    }

    /**
     * Returns an array of all the ActionListeners added to this
     * IOPane with {@link #addActionListener}.
     *
     * @return all of the ActionListeners added or an empty array if no listeners have been added
     */
    public ActionListener[] getActionListeners() {
        return input.getActionListeners();
    }

    /**
     * Sets the background color of this IOPane, its output area and
     * its input field.
     *
     * @param c the desired background Color
     */
    public final void setBackground(Color c) {
        super.setBackground(c);

        Component[] comp = getComponents();
        for (int i = 0; i < comp.length; i++) {
            comp[i].setBackground(c);
        }
    }

    /**
     * Fetches the model associated with the output area.
     *
     * @return the model of the output area
     */
    public Document getDocument() {
        return jta.getDocument();
    }

    /**
     * Fetches the command history of the input field.
     *
     * @return the command history
     */
    public History getHistory() {
        return history;
    }


    /**
     * Sets the maximum number of lines in the output area. When the text in
     * the output area exceeds the limit set by this method. Lines are removed
     * from its beginning. The initial limit is 100 lines.
     *
     * @param lines the line limit, if lines is negative the number of
     *              lines is unlimited
     * @see #getMaxLines
     */
    public void setMaxLines(int lines) {
        maxLines = lines;
        trim();
    }

    /**
     * Returns the size limit of the output area of this IOPane.
     *
     * @return the maximum number of lines in the ouput area
     * @see #setMaxLines
     */
    public int getMaxLines() {
        return maxLines;
    }

    /**
     * Adds the specified action listener to receive action events from
     * the input textfield.
     *
     * @param a the action listener to be added
     */
    public void addActionListener(ActionListener a) {
        input.addActionListener(a);
    }

    /**
     * Appends the given text to the end of the document of the output area.
     * @param s the text to append
     * @see javax.swing.JTextArea#append
     */
    public void append(String s) {
        jta.append(s);
    }

    /**
     * Constructs the GUI of this IOPane.
     */
    private void buildGui() {

        Border eb = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        jsp.setBorder(eb);
        jta.setBorder(eb);
        jsp.setViewportView(jta);
        jta.setLineWrap(true);
        jta.setEditable(false);
        input.setBorder(eb);

        //Assemble the application GUI
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        prompt = new JLabel(" >");
        prompt.setOpaque(true);

        // TextArea
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 10;
        gbc.weightx = 1.;
        gbc.weighty = 1.;
        add(jsp, gbc);

        // Input Prompt
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.weighty = 0.;
        gbc.weightx = 0.;
        gbc.gridwidth = 1;
        add(prompt, gbc);
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(input, gbc);
        setBackground(Color.white);
    }

    /**
     * Removes the specified action listener so that it no longer receives
     * action events from the input textfield.
     *
     * @param a the action listener to be removed
     */
    public void removeActionListener(ActionListener a) {
        input.removeActionListener(a);
    }

    /**
     * Returns the input text field of this IOPane.
     *
     * @return the input text field
     */
    protected JTextField getInput() {
        return input;
    }

    /**
     * Returns the output area of this IOPane.
     *
     * @return the output area.
     */
    protected JTextArea getOutputArea() {
        return jta;
    }

    /**
     * Returns the Label preceding the input field of this IOPane.
     *
     * @return the JLabel preceding the input field
     */
    protected JLabel getPrompt() {
        return prompt;
    }

    /**
     * Returns the ScrollPane containing the output area of this IOPane.
     *
     * @return the scroll pane of the output area
     */
    protected JScrollPane getScrollPane() {
        return jsp;
    }

    /**
     * Removes surplus lines from the start of the text in the output area.
     */
    private void trim() {
        if (getMaxLines() > 0 && jta.getLineCount() > getMaxLines()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        synchronized (jta.getDocument()) {

                            int linesToDelete = jta.getLineCount() -
                                                getMaxLines() - 2;
                            jta.getDocument().remove(0,
                                                     jta.getLineEndOffset(
                                                                 linesToDelete));
                            jta.getDocument().notifyAll();
                        }
                    } catch (BadLocationException ex) {
                        System.err.println(ex);
                        System.err.flush();
                    }
                }
            });
        }
    }


    /** Minimum requirements for a command line history
        for the input field of an IOPane. You can navigate this history step by
        step using the next() and previous() methods. */
    public static interface History {

        /**
         * Returns the ActionListener that adds items to this history.
         * It should usually {@link #add} the value returned
         * by {@link ActionEvent#getActionCommand()}.
         *
         * @return an action listener that adds new items to this history
         */
        public ActionListener getAddListener();

        /**
         * Sets the maximum number of items this history can hold.
         *
         * @param maxitems the maximum number of items
         * @see #getLength
         */
        public void setLength(int maxitems);

        /**
         * Returns the maximum number of items this history can hold.
         *
         * @return the maximum number of items
         * @see #setLength
         */
        public int getLength();

        /**
         * Adds the specified item to this history. If item is not <code>null</code>
         * the next call to next() must return item.
         * @param item the item to add
         */
        public void add(String item);

        /**
         * Removes all items from this history.
         */
        public void clear();

        /**
         * If there was no call to next() after the last {@link #add}
         * this method will return the last item added to this history. Otherwise, it returns
         * the item added before the item returned by the last call to next() or
         * {@link #previous()}. It returns <code>null</code> if no such item exists.
         * @return the next item in this history or <code>null</code> at its end
         */
        public String next();

        /**
         * Returns the item added after the item returned by the last call to
         * next() or previous(). Even if no such item exists this method must
         * return a non-null value, preferably the empty string.
         *
         * @return the previous item in this history
         */
        public String previous();

        /**
         * Resets the current position in this history such that the next item
         * returned by next()
         * will be the item last added or null if no such item exists.
         */
        public void resetPointer();
    }

    /** Default implementation of the IOPane.History interface. **/
    public static final class VectorHistory extends Vector implements History {

        private final ActionListener addListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() != null &&
                            !e.getActionCommand().equals("")) {
                    VectorHistory.this.add(e.getActionCommand());
                }
            }
        };

        private int currentItem = 0;
        private int length = 100;

        /**
         * Creates a new VectorHistory object.
         */
        public VectorHistory() {
            super(10);
            super.add("");
        }

        /**
         * Returns the ActionListener that adds items to this history.
         * It should usually {@link #add} the value returned
         * by {@link ActionEvent#getActionCommand()}.
         *
         * @return an action listener that adds new items to this history
         */
        public ActionListener getAddListener() {
            return addListener;
        }

        /**
         * Sets the maximum number of items in the history. Default is 100.
         * when maxitems is &lt;= 0 the history has unlimited size.
         * @param maxitems the maximum number of items
         * @see #getLength
         */
        public void setLength(int maxitems) {
            length = maxitems;
            if (length > 0 && size() > length) {
                setSize(length + 1);
                trimToSize();
            }
        }

        /**
         * Returns the maximum number of items this history can hold.
         *
         * @return the maximum number of items, default is 100
         * @see #setLength
         */
        public int getLength() {
            return length;
        }

        /**
         * Adds the specified item to this history.
         * The next call to next() must return item.
         * @param item the item to add
         * @throws NullPointerException if item is <code>null</code>
         */
        public void add(String item) {
            item.length(); //throws a NullPointerException if item is null
            if (length > 0 && size() == length + 1) {
                remove(size() - 1);
            }
            add(1, item);
            currentItem = 0;
        }

        /**
         * Removes all items from this history.
         */
        public void clear() {
            setSize(1);
        }

        /**
         * If there was no call to next() after the last {@link #add}
         * this method will return the last item added to this history. Otherwise, it returns
         * the item added before the item returned by the last call to next() or
         * {@link #previous()}. It returns <code>null</code> if no such item exists.
         * @return the next item in this history or <code>null</code> at its end
         */
        public String next() {
            if (currentItem + 1 < size()) {
                return (String) get(++currentItem);
            } else {
                return null;
            }
        }

        /**
         * Returns the item added after the item returned by the last call to
         * next() or previous(). Even if no such item exists this method must
         * return a non-null value, preferably the empty string.
         *
         * @return the previous item in this history
         */
        public String previous() {
            if (currentItem == 0) {
                return (String) get(0);
            } else {
                return (String) get(--currentItem);
            }
        }

        /**
         * Resets the current position in this history such that the next item
         * returned by next()
         * will be the item last added or null if no such item exists.
         */
        public void resetPointer() {
            currentItem = 0;
        }
    }
}