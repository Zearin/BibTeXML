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

import de.mospace.swing.event.MessageEvent;
import de.mospace.swing.event.MessageListener;

/**
 * A status bar that gets the messages to display via MessageEvents.
 * @see de.mospace.swing.event.MessageEvent
 *
 *  @version $Revision$ ($Date$)
 *  @author Moritz Ringler
 */
public class ListeningStatusBar extends StatusBar implements MessageListener {

    /**
     * Creates a new ListeningStatusBar object.
     */
    public ListeningStatusBar() {
        super();
    }

    /**
     * Creates a new ListeningStatusBar object.
     *
     * @param timeout DOCUMENT ME!
     */
    public ListeningStatusBar(int timeout) {
        super(timeout);
    }

    public void messageSent(MessageEvent e) {
        if(e.getTimeout() == MessageEvent.DEFAULT_TIMEOUT){
            setStatus(e.getMessage());
        } else {
            setStatus(e.getMessage(), e.getTimeout());
        }
    }
}
