package de.mospace.swing.text;

/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
 * $Id: FullTextStringCellRenderer.java,v 1.4 2007/02/18 14:19:15 ringler Exp $
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

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Multiline string renderer for JTable. The multiline string is abbreviated
 * to the first at most 29 characters of its first line and three dots are
 * appended, if necessary. The resulting string is displayed by the
 * DefaultTableCellRenderer.
 *
 * @version $Revision: 1.4 $ ($Date: 2007/02/18 14:19:15 $)
 * @author Moritz Ringler
 * @see de.mospace.lang.FullTextString
 */
public class FullTextStringCellRenderer extends DefaultTableCellRenderer {
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    public FullTextStringCellRenderer() {
        super();
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
        String shortstr = (value==null)? "":value.toString();
        int iend   = shortstr.length();
        int inull  = shortstr.indexOf(0);
        int ibreak = shortstr.indexOf('\n');
        int i      = Math.min(iend, 29);
        if ((inull > -1) && (inull < i)){
            i = inull;
        }
        if ((ibreak > -1) && ibreak < i){
            i = ibreak;
        }
        shortstr = shortstr.substring(0,i);
        if(i<iend && shortstr.length() != 0){
            shortstr = shortstr.concat("...");
        }
        return super.getTableCellRendererComponent(table, shortstr, isSelected,
                hasFocus, row, column);
    }
}
