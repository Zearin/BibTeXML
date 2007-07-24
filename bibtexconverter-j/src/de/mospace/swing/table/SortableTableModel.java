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

    /** A row-sortable TableModel where sorting is managed by
     * translating between two sets of indices, an
     * index set identifying the table entries (entries)
     * and another index set representing the current position of
     * an entry in the table (rows).<p>
     * All TableModel methods accept and return
     * rows because this is what the associated JTables need. If one wants
     * to access the same entry in two SortableTableModels the row numbers
     * have to be transformed accordingly. E. g.
     * <code>
     * this.setValueAt(anotherSTM.getValueAt(anotherSTM.getRows(getEntries(row)),col), row, col)
     * </code>
     * @author Moritz Ringler
     * @version $Revision$ ($Date$)
     */
public interface SortableTableModel extends TableModel{
    /** Constant for ascending sort order */
    final static int ASCENDING = 1;
    /** Constant for descending sort order */
    final static int DESCENDING = -1;

    /** Sorts the entries in this TableModel according to the criteria
     *  set by {@link #setSortBy}.
     **/
    public void sort();

    /** Shuffles the entries in this TableModel.**/
    public void randomize();

    /** Sets the column with top sorting precedence
    * and the way it is to be sorted. Implementations
    * can but do not need to remember previous calls to this method to
    * determine what to do when two entries have equal fields
    * in this column.
    * @param col the table column
    * @param asc true for ascending order
    */
    public void setSortBy(int col, boolean asc);

    /** Sets the column with top sorting precedence. If <code>col</code> is
    * already the column with top sorting precedence the sort order will
    * be reversed, otherwise the entries in this model will be sorted in
    * ascending order.<p>
    * Implementations can but do not need to remember previous calls
    * to this method to determine what to do when two entries have equal fields
    * in this column.
    *
    * @param col the table column
    *
    * @see #setSortBy(int, boolean)
    */
    public void setSortBy(int col);

    /** Gets the column with top sorting precedence.
    *
    * @return the column by which this table model is sorted.
    */
    public int getSortingColumn();

    /** Returns whether the model is currently sorted or not. **/
    public boolean isSorted();

    /** Gets the columns by which this table is sorted. The returned array
    * holds the information used for sorting the table.
    * The array holds n pairs of integers in the order of decreasing sorting
    * precedence. The first integer in each pair is a column number, if it is
    * -1 this element and all following elements must be ignored. The second integer
    * is either <code>ASCENDING</code> or <code>DESCENDING</code>.
    * @return an array of {column, order} pairs with decreasing sorting precedence
    */
    public int[][] getSortOrder();

    /** Gets the current row number of an entry.*/
    public int getRows(int entry);

    /** Gets the current row numbers for a list of entries.*/
    public int[] getRows(int[] entries);

    /** Gets the entry represented by the specified row in the current sorting.*/
    public int getEntries(int row);

    /** Gets the entries represented by the specified rows in the current sorting.*/
    public int[] getEntries(int[] rows);
}
