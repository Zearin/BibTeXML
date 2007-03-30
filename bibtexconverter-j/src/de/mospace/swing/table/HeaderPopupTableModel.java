package de.mospace.swing.table;

/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
 * $Id: HeaderPopupTableModel.java,v 1.5 2007/02/18 14:20:24 ringler Exp $
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

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.table.TableModel;

/** A TableModel that provides a context menu for column headers in the
* associated JTable.
*
* @version $Revision: 1.5 $ ($Date: 2007/02/18 14:20:24 $)
* @author Moritz Ringler
**/
public interface HeaderPopupTableModel extends TableModel{
    /** Returns the header context menu for the column on which the
    specified MouseEvent occurred. */
    public JPopupMenu getHeaderPopup(MouseEvent e);
}
