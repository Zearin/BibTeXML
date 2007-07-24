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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

/** A simplified progress monitor. It calls "close()" whenever it is
 * terminated. By overriding the close() method you can thus handle any form of
 * termination (e.g. for re-enabling another window).
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 */
 public class SimpleProgressMonitor{
     private JDialog dialog;
     private JLabel progressText;
     private JProgressBar progressBar;
     private boolean canceled;

     /** Constructs a new ProgressMonitor dialog and makes it visible.
      * @param f parent window
      * @param title title of the dialog
      * @param note initial text to display
      * @param min integer corresponding to 0% progress
      * @param max integer corresponding to 100% progress
      * @deprecated use SimpleProgressMonitor(Frame f, String title, String note) instead
     **/
     public SimpleProgressMonitor(Frame f, String title, String note,
     int min, int max){
         reset(f,title,note,min,max);
     }

     public SimpleProgressMonitor(Frame f, String title, String note){
         reset(f,title,note);
     }

     public void reset(Frame f, String title, String note){
         reset(f,title,note,0,100);
     }

     /** Resets an existing progress monitor.
      * @param f parent window
      * @param title title of the dialog
      * @param note initial text to display
      * @param min integer corresponding to 0% progress
      * @param max integer corresponding to 100% progress
     **/
     private final void reset(Frame f, String title, String note,
     int min, int max){
         canceled = false;
         progressText = new JLabel((note == null)? "": note);
         progressText.setPreferredSize(new Dimension(300,20));
         progressBar = new JProgressBar(min, max);
         progressBar.setPreferredSize(new Dimension(290,20));
         JButton cancel = new JButton(UIManager.getString("OptionPane.cancelButtonText"));
         cancel.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 cancel();
             }
         });
         dialog = new JDialog(f, title, false){
             protected void processWindowEvent(WindowEvent e){
                 if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                     cancel();
                 }
                 super.processWindowEvent(e);
             }
         };
         Container dcp = dialog.getContentPane();
         dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         dcp.setLayout(new BorderLayout());
         ((JComponent) dcp).setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         if(note != null){
              dcp.add(progressText, BorderLayout.NORTH);
         }
         dcp.add(progressBar, BorderLayout.CENTER);
         JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
         buttons.add(cancel);
         dcp.add(buttons, BorderLayout.SOUTH);
         dialog.pack();
         dialog.setLocationRelativeTo(f);
         //dialog.setVisible(true);
     }

     public void setVisible(boolean visible){
         dialog.setVisible(visible);
     }

     public boolean isVisible(){
         return dialog.isVisible();
     }

     /** Updates the progress bar with the indicated value.
     * @see javax.swing.JProgressBar#setValue(int)
     */
     public void setProgress(int i){
         if(i >= progressBar.getMaximum()){
             close();
         }
         if(i > progressBar.getValue()){
             progressBar.setValue(i);
         }
     }

     /** Returns the current value of the progress bar.
     * @see javax.swing.JProgressBar#getValue()
     */
     public int getProgress(){
         return progressBar.getValue();
     }

     /** Changes the text displayed in the dialog window. **/
     public void setNote(String s){
         progressText.setText(s);
     }

     public void setMaximum(int max){
         progressBar.setMaximum(max);
     }

     public void setMinimum(int min){
         progressBar.setMinimum(min);
     }

     /** Returns whether the dialog has been cancelled.
     * @see #cancel()
     **/
     public boolean isCanceled(){
         return canceled;
     }

     /** Cancels the progress monitor dialog. */
     public void cancel(){
         close();
         canceled = true;
     }

     /** Closes the progress monitor dialog. */
     public void close(){
         dialog.setVisible(false);
         dialog.dispose();
     }
 }