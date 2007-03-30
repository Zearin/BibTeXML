/* $Id: StatusBar.java,v 1.6 2006/03/14 11:49:18 ringler Exp $
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.Timer;

/** A status bar that is automatically cleared after some time.
 * @author Moritz Ringler
 * @version $Revision: 1.6 $ ($Date: 2006/03/14 11:49:18 $)
 */
public class StatusBar extends JLabel{
      private Timer clearTimer;
      private ActionListener clearTimerActionListener;
      private int defaultTimeout;
      /** initial timeout */
      final public static int INITIAL_TIMEOUT=1000;

      /** Constructs a new instance of this class. Equivalent to
       * StatusBar(StatusBar.INITIAL_TIMEOUT).
       **/
      public StatusBar(){
        this.setFont(new java.awt.Font("Dialogue", 0, 9));
        this.setBackground(Color.white);
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEtchedBorder());
        clear();
        clearTimerActionListener = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
            clear();
          }
        };
        defaultTimeout=INITIAL_TIMEOUT;
        clearTimer = new Timer(defaultTimeout,clearTimerActionListener);
        clearTimer.setRepeats(false);
      }

      /** Constructs a new instance of this class. Status bar messages
       * are cleared after <code>timeout</code> milliseconds by default.
       * @param timeout default timeout in milliseconds
       **/
      public StatusBar(int timeout){
        this.setFont(new java.awt.Font("Dialogue", 0, 9));
        this.setBackground(Color.white);
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEtchedBorder());
        this.clear();
        clearTimerActionListener = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
            clear();
          }
        };
        defaultTimeout=timeout;
        clearTimer = new Timer(defaultTimeout,clearTimerActionListener);
        clearTimer.setRepeats(false);
      }

      /** Shows the spefied message in the status bar for
       * the time specified by {@link #setDefaultTimeout(int timeout)}.
       * @param status message that will be displayed
       */
      public void setStatus(String status){
        if(clearTimer.isRunning()){
          clearTimer.stop();
        }
        this.setText(status);
        clearTimer.setDelay(defaultTimeout);
        clearTimer.start();
      }

      /**
       * Shows the specified message and clears the status bar after
       * the specified delay.
       * @param status message that will be displayed
       * @param timeout time the message will remain visible (milliseconds)
       */
      public void setStatus(String status, int timeout){
        if(clearTimer.isRunning()){
          clearTimer.stop();
        }
        this.setText(status);
        if (timeout>0){
          clearTimer.setDelay(timeout);
          clearTimer.start();
        }
      }

      /** Sets the default time a status bar message remains visible.
       * @param timeout time in milliseconds
       */
      public void setDefaultTimeout(int timeout){
        defaultTimeout = timeout;
      }

      /** Clears the status bar. */
      public void clear(){
        this.setText(" ");
      }
}
