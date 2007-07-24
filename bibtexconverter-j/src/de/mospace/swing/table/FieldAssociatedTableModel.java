package de.mospace.swing.table;

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

import javax.swing.table.TableModel;

/** A TableModel whose columns are associated with data fields
 *  represented by unique int values.
 * @deprecated This class is no longer used by mp3dings, integer
 * field identifiers have been replaced by Field objects.
 *
 * @version $Revision$ ($Date$)
 * @author Moritz Ringler
 **/
public interface FieldAssociatedTableModel extends TableModel{
    /** Returns the identifier of the field currently associated
     * with the specified table column.
     *  @param col the column number for which to retrieve the field
     *  @return the field identifier or a value of -1 if col is invalid
     **/
    public int getColumnID(int col);

    /** Returns the column number of the column currently associated with
     * the specified field.
     * @param id an int value identifying an allowed data field
     * @return the column number of the associated column or a value of
     *         -1 if no column is associated with that field identifier.
     **/
    public int getColForID(int id);

    /** Returns the fields that can be displayed in a table.
     * @return An array of integer field identifiers.
     */
    public int[] getAllowedFields();

     /** Returns the fields that must always be displayed.
     * @return An array of integer field identifiers.
     */
    public int[] getPersistentFields();

    /** Returns the fields that are displayed by default.
     * @return An array of integer field identifiers.
     */
    public int[] getDefaultFields();

    /** Returns the editable property for an allowed field.*/
    public boolean getEditable(int fieldID);

    /** Returns the fields that currently have table columns associated with
     * them.
     * @return An array of integer field identifiers.
     */
    public int[] getFields();
}
