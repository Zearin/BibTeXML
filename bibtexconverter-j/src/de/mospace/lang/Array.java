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

import java.util.Arrays;
 
/**
 * Array utilities.
 * @author Moritz Ringler
 * @version $Revision$ ($Date$)
 */
public class Array{
    private Array(){
    }
    /**Joins the elements of an Object array to a character-delimited String.
     * No warning will
     * be issued if one of the array elements contains the delimiter.
     * @param arr the array to join
     * @param delim the delimiter to use
     * @return a string made up of the string representations of the array elements separated by the delimiter character
     */
    public static String join(Object[] arr, char delim){
        if (arr.length == 0){
            return "";
        }
        StringBuffer jsb = new StringBuffer(arr.length*3);
        jsb.append(arr[0]);
        for (int i=1; i<arr.length; i++){
            jsb.append(delim);
            jsb.append(arr[i]);
        }
        return jsb.toString();
    }

    /**Joins the elements of an int array to a character-delimited String.
     * No warning will
     * be issued if one of the array elements contains the delimiter.
     * @param arr the array to join
     * @param delim the delimiter to use
     * @return a string made up of the string representations of the
     * array elements separated by the delimiter character
     */
    public static String join(int[] arr, char delim){
        if (arr.length == 0){
            return "";
        }
        StringBuffer jsb = new StringBuffer(arr.length*3);
        jsb.append(arr[0]);
        for (int i=1; i<arr.length; i++){
            jsb.append(delim);
            jsb.append(arr[i]);
        }
        return jsb.toString();
    }

    /**Converts a byte array to a Hex String.
    * @param b a non-null byte array 
    * @return the input array as a sequence of Hex digits
    */
    public static String byteArrayToString(byte[] b){
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i=0; i<b.length; i++){
            //sb.append('$');
            String s = Integer.toHexString((new Byte(b[i])).intValue() & 0xff);
            for(int j=0; j<(2-s.length()); j++){
              sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /** Returns the continuous sequences of integers contained in the input
    * array. The input array is sorted and for each continous sequence
    * of integers that it contains a 2-element array made up of its lower
    * and upper bounds is added to the result.
    * @param numbers the set of integers to analyze
    * @return <code>null</code> if <code>numbers</code> is <code>null</code>
    * or empty. Otherwise an ordered array of pairs of integers, which are
    * the lower and upper bounds of the continuous sequences of integers
    * contained in the input.
    */
    public static int[][] getIntervals(int[] numbers){
        if(numbers == null || numbers.length == 0){
            return null;
        }
        Arrays.sort(numbers);
        int[][] rawresult = new int[numbers.length][2];
        rawresult[0] = new int[]{numbers[0],numbers[0]};
        int j = 0;
        for (int i=1; i<numbers.length; i++){
            if(++rawresult[j][1] != numbers[i]){
                rawresult[j][1]--;
                rawresult[++j] = new int[]{numbers[i], numbers[i]};
            }
        }
        /* truncate array */
        if (++j<rawresult.length){
            int[][] result = new int[j][2];
            System.arraycopy(rawresult,0,result,0,j);
            return result;
        } else {
            return rawresult;
        }
    }

    /** Extracts a sub-array from a double array.
    * @param src a non-null double array
    * @param begin the start of the sub-array to extract
    * @param length the length of the sub-array to extract
    * @return a new double array 
    * <code>{src[begin], ..., src[begin + length - 1]}</code> 
    */
    public static double[] slice(double[] src, int begin, int length){
        double[] result = new double[length];
        System.arraycopy(src,begin,result,0,length);
        return result;
    }
}
