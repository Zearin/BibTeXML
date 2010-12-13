package de.mospace.swing.event;

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

import java.util.EventObject;

/**
 * An event that informs listeners of one or more changes in the data
 * of the source.
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 */
public class DataChangeEvent extends EventObject{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6584824184768549764L;
	private int numOfChanges = 1;
    
    /** Creates a new DataChangeEvent for a single change in the data of the
    * source.
    * @param source the event source
    */
    public DataChangeEvent(Object source){
        super(source);
    }

    /** Creates a new DataChangeEvent for one or more changes in the data of the
    * source.
    * @param source the event source
    * @param changeCount the number of changes in the data
    */
    public DataChangeEvent(Object source,
            int changeCount){
        super(source);
        numOfChanges = changeCount;
    }

    /** Returns the change count for this DataChangeEvent.
    * @return the number of changes in the data
    */
    public int getChangeCount(){
        return numOfChanges;
    }
}
