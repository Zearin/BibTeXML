/* $Id: TableSorter.java,v 1.9 2007/02/18 14:20:24 ringler Exp $
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
package de.mospace.swing.table;

import java.util.Date;
import java.util.Random;

/**
 * A Table sorter along the lines of the TableSorter from Sun's Swing Tutorial.
 * For a given {@link SortableTableModel} its {@link #sort sort} and
 * {@link #randomize randomize} methods produce a one-to-one mapping
 * between entries and row numbers.
 *
 * @version $Revision: 1.9 $ ($Date: 2007/02/18 14:20:24 $)
 * @author Moritz Ringler
 **/
public class TableSorter {
    /** index of the row-for-entry mapping in the arrays returned by the
    * {@link #sort sort} and {@link #randomize randomize} methods. */
    public final static int FWD=0;
    /** index of the entry-for-row mapping in the arrays returned by the
    * {@link #sort sort} and {@link #randomize randomize} methods. */
    public final static int INV=1;
    private int indexes[];
    private int compares;
    private SortableTableModel model;
    private int[][] sortingColumns;

    /** Constructs a new TableSorter. The same table sorter can be used to sort
    * multiple table models.
    */
    public TableSorter(){
        //sole constructor
    }

    private int compareRowsByColumn(int row1, int row2, int column){
        Class type = model.getColumnClass(column);
        SortableTableModel data = model;

        /* SortableTableModel expects a (sorted row)-index as its
         * getValueAt-methods first argument, so we have to make
         * (sorted row)-indexes from the (unsorted row)-indexes
         */
        int row1s = model.getRows(row1);
        int row2s = model.getRows(row2);

        /* Get the values */
        Object o1 = data.getValueAt(row1s, column);
        Object o2 = data.getValueAt(row2s, column);

        double result = 0;
        /* If both values are identical, return 0. */
        if (o1 == o2) { //objects may be null at this stage ->don't use equals()
            result = 0;

        /* Define null less than everything. */
        } else if (o1 == null) {
            result = -1;
        } else if (o2 == null) {
            result = 1;
        } else if (o1.equals(o2)){
            return 0;

        /* Compare numbers numerically */
        } else if (Number.class.isAssignableFrom(type)) {
            Number n1 = (Number) o1;
            Number n2 = (Number) o2;
            result = n1.doubleValue() - n2.doubleValue();

        /* Compare dates numerically */
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) o1;
            Date d2 = (Date) o2;
            result = d1.getTime() - d2.getTime();
        } else if (type == String.class) {
        /* mr numeric comparison for numeric strings */
            String s1 = (String) o1;
            String s2 = (String) o2;
            try{
                result = Double.parseDouble(s1) - Double.parseDouble(s2);
            } catch (NumberFormatException ex){
                /* mr: changed to non-case-sensitive comparison */
                result = (s1.toLowerCase()).compareTo(s2.toLowerCase());
            }
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) o1;
            Boolean bool2 = (Boolean) o2;
            if (bool1.equals(bool2)) {
                result = 0;
            } else if (bool1.booleanValue()) { // Define false < true
                result = 1;
            } else {
                result = -1;
            }
        } else {
            String s1 = o1.toString();
            String s2 = o2.toString();
            /* mr: changed to non-case-sensitive comparison */
            result = (s1.toLowerCase()).compareTo(s2.toLowerCase());
        }
        return (result == 0)
                ? 0
                : ( (result > 0)? 1 : -1 );
    }

    /** Returns {-1|0|1} if row1 is {less than|equal|greater than} row2
    * in the current sorting.
    * @param row1 the index of the first row in the comparison
    * @param row2 the index of the other row in the comparison
    **/
    private int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.length; level++) {
            int result = compareRowsByColumn(row1, row2, colAtLevel(level));
            if (result != 0) {
                return isAscendingAtLevel(level)? result : -result;
            }
        }
        return 0;
    }

    private boolean isAscendingAtLevel(int i){
        return (sortingColumns[i][1] == SortableTableModel.ASCENDING);
    }

    private int colAtLevel(int i){
        return (sortingColumns[i][0]);
    }

    /** (Re-)initializes this sorter with the identity mapping. **/
    private void reallocateIndices() {
        int rowCount = model.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    /** Performs sorting of the table. **/
    private void n2sort() {
        int n = model.getRowCount();
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    private void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high)/2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.

        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            }
            else {
                to[i] = from[q++];
            }
        }
    }

    /** Exchanges rows i and j in the current mapping.
    * @param i the index of the first row
    * @param j the index of the row to swap the first row with
    */
    private void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    /** Sorts the specified table model.
    * @param m the data to sort
    * @return a 2D array holding the new row-for-entry mapping at
    *         index FWD and the inverse mapping at index INV.
    **/
     public synchronized int[][] sort(SortableTableModel m){
        model = m;
        reallocateIndices();
        sortingColumns = m.getSortOrder();
        compares = 0;
        shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
        int n = indexes.length;
        int[][] mapping = new int[2][n];
        mapping[INV]=(int[]) indexes.clone();
        for (int i=0;i<n;i++){
            mapping[FWD][indexes[i]]=i;
        }
        return mapping;
    }

    /** Returns a random row-entry mapping for the specified SortableTableModel.
    * @param m the data to randomize
    * @return a 2D array holding the new row-for-entry mapping at
    *         index FWD and the inverse mapping at index INV.
    */
    public synchronized int[][] randomize(SortableTableModel m){
        model = m;
        /* initialize indexes with the identity mapping */
        reallocateIndices();
        int i = indexes.length;
        if(i==1){
            return new int[][]{{0},{0}};
        }
        int[][] mapping = new int[2][i];
        /* randomize using the Fisher-Yates algorithm */
        Random rnd = new Random();
        while(--i>0){
            /* Swap element i with a random element with lower index. */
            swap(i, rnd.nextInt(i));
            /* Invert the new mapping. */
            mapping[FWD][indexes[i]]=i;
        }
        mapping[INV]=(int[]) indexes.clone();
        return mapping;
    }
}
