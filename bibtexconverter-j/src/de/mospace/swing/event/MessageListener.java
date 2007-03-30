package de.mospace.swing.event;

/* de.mospace.swing library
 * Copyright (C) 2005 Moritz Ringler
 * $Id: MessageListener.java,v 1.4 2007/02/18 14:20:23 ringler Exp $
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

import java.util.EventListener;

/** A listener for {@link MessageEvent MessageEvents}.
 *  @see de.mospace.swing.event.MessageEvent
 *
 * @version $Revision: 1.4 $ ($Date: 2007/02/18 14:20:23 $)
 * @author Moritz Ringler
 **/
public interface MessageListener extends EventListener{
    
    /** Performs some action when a MessageEvent occurred.
    * @param e a MessageEvent
    */  
    public void messageSent(MessageEvent e);
}
