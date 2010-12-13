package de.mospace.swing.event;

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

import java.util.EventObject;

/**
 * A simple event consisting of a String message and a timeout
 * for the duration of the message display.
 * @version $Revision$ ($Date$)
 * @author Moritz Ringler
 */
public class MessageEvent extends EventObject{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5893899514373624789L;

	/** Type argument
    * for the 3- and 4-argument MessageEvent constructors -
    * for information messages. */
    public final static int TYPE_INFO = 0;
    
    /** Type argument
    * for the 3- and 4-argument MessageEvent constructors -
    * for error messages. */
    public final static int TYPE_ERROR     = 1;
    
    /** Type argument
    * for the 3- and 4-argument MessageEvent constructors -
    * for warning messages. */
    public final static int TYPE_WARNING   = 2;
    
    /** Timeout argument
    * for the 4-argument MessageEvent constructor
    * that tells MessageListeners to use their respective
    * default timeouts. */
    public final static int DEFAULT_TIMEOUT = -1;
    
    /** Timeout argument
    * for the 4-argument MessageEvent constructor
    * that tells MessageListeners to not clear the message display. */
    public final static int NO_CLEAR = 0;
    private String message = "";
    private int timeout = DEFAULT_TIMEOUT;
    private int type = TYPE_INFO;

    /** Constructs a new message event with the specified source, and message.
    * Equivalent to
    * <code>MessageEvent(source, message, TYPE_INFO, DEFAULT_TIMEOUT)</code>
    * @param source the source for the new event
    * @param message the message for the new event
    */
    public MessageEvent(Object source, String message){
        super(source);
        this.message = message;
    }

    /** Constructs a new message event with the specified source, message, and
    * type. Equivalent to
    * <code>MessageEvent(source, message, type, DEFAULT_TIMEOUT)</code>
    * @param source the source for the new event
    * @param message the message for the new event
    * @param type the type of message for the new event. One of
    * TYPE_INFO, TYPE_ERROR, TYPE_WARNING
    */
    public MessageEvent(Object source, String message, int type){
        this(source, message);
        this.type = type;
    }
    
    /** Constructs a new message event with the specified source, message,
    * type, and timeout.
    * @param source the source for the new event
    * @param message the message for the new event
    * @param type the type of message for the new event. One of
    * TYPE_INFO, TYPE_ERROR, TYPE_WARNING
    * @param timeout the time period during which a listener should display
    * the message
    */
    public MessageEvent(Object source, String message, int type, int timeout){
        this(source, message, type);
        this.timeout = timeout;
    }

    /** Returns the message of this message event.
    * @return the message of this message event.
    */
    public String getMessage(){
        return message;
    }

    /** Returns the timeout for this message event.
    * @return the timeout for this message event.
    */
    public int getTimeout(){
        return timeout;
    }
}
