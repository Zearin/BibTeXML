package de.mospace.lang;

/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
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

import java.util.logging.Handler;

/** Classes implementing this interface write log messages to a
* {@link java.util.logging.Handler}.
*/
public interface LogSupport{
    /** Registers the specified handler to handle log events
    * emitted by an implementation of this class.
    * @param lh a Handler for handling log messages emitted by this object
    */
    public void addLogHandler(Handler lh);
}