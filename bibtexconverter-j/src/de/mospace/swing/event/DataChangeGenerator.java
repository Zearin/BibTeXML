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
/**
 * @deprecated
 * @version $Revision$ ($Date$)
 * @author Moritz Ringler
 **/
@Deprecated
public interface DataChangeGenerator{
    public void addDataChangeListener(DataChangeListener l);
    public void removeDataChangeListener(DataChangeListener l );
    public void fireDataChanged(int n);
    public void fireDataChanged();
}
