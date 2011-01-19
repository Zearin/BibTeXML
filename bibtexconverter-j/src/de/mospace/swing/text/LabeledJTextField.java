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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.InputVerifier;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** A JTextField with a label on the left.
 *
 * @version $Revision$ ($Date$)
 * @author Moritz Ringler
**/
public class LabeledJTextField extends JPanel{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6304773250787813813L;
	private final JTextField jTextField;

    /** Constructs a new text field with the specified label
    * and initial text.
    * @param lbel the label for the text field
    * @param text the initial text
    */
    public LabeledJTextField(String lbel,String text){
      FlowLayout flowLayout = new FlowLayout();
      flowLayout.setAlignment(FlowLayout.LEFT);
      jTextField=new JTextField();
      jTextField.setMinimumSize(new Dimension(21, 20));
      jTextField.setPreferredSize(new Dimension(35, 20));
      jTextField.setText(text);
      JLabel jLabel = new JLabel();
      jLabel.setText(lbel);
      this.setMinimumSize(new Dimension(112, 15));
      this.setLayout(flowLayout);
      this.add(jTextField,null);
      this.add(jLabel,null);
    }

    /** Adds an action listener to the text field.
    * @param al the action listener to add
    * @see javax.swing.JTextField#addActionListener
    */
    public void addActionListener(ActionListener al){
      jTextField.addActionListener(al);
    }

    /** Sets an input verifier for the text field
    * @param iv the new inout verifier for the text field
    * @see javax.swing.JTextField#setInputVerifier
    */
    @Override
    public void setInputVerifier(InputVerifier iv){
      jTextField.setInputVerifier(iv);
    }

    /** Returns the current text of this labeled text field.
    * @return the current text
    * @see javax.swing.JTextField#getText
    */
    public String getText(){
      return jTextField.getText();
    }
}
