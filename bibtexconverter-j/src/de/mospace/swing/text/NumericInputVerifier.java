package de.mospace.swing.text;

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

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/** An input verifier that checks if a text component's text can be interpreted
*  as a particular sub-class of number and if it falls into the allowed
* range of numbers.
* @deprecated use GeneralInputVerifier instead
*
* @version $Revision$ ($Date$)
* @author Moritz Ringler
*/
public class NumericInputVerifier extends InputVerifier implements java.io.Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3369466113721179363L;
	private Class  myClass = Double.class;
    private Method method;
    private Constructor constructor;
    private Comparable min = null;
    private Comparable max = null;
    private transient Exception ex = null;
    
    /** Constructs a new NumericInputVerifier for the given 
    * sub-class of Number and the given limits. 
    * @param c the sub-class of number that input will be interpreted as 
    * @param min lower limit. null for none.
    * @param max upper limit. null for none.
    * @throws IllegalArgumentException if <code>c</code> does not implement
    * Number, or if it does not have a static
    * <code>valueOf(String)</code> method or a single argument string 
    * constructor.
    * @throws ClassCastException if
    * <code>min</code> and <code>max</code> are not <code>null</code>
    * or instances of <code>c</code>. 
    */
    public NumericInputVerifier(Class c, Comparable min, Comparable max){
        setNumberClass(c);
        setUpperLimit(max);
        setLowerLimit(min);
    }
    
    /** Constructs a new NumericInputVerifier for the full range of numbers
    * in the given sub-class of Number. 
    * @param c the sub-class of number that input will be
    * interpreted as 
    * @throws IllegalArgumentException if <code>c</code> does not implement
    * Number, or if it does not have a static
    * <code>valueOf(String)</code> method or a constructor with
    * a single String argument
    */
    public NumericInputVerifier(Class c){
        setNumberClass(c);
    }
    
    /** Returns the class against which input will be checked.
    * @see #setNumberClass
    */
    public Class getNumberClass(){
        return myClass;
    }
    
    /** Sets the class against which input will be checked. This will
    * automatically reset the allowed range to the full range of numbers 
    * in this class.
    * @param c the sub-class of number that input will be interpreted as 
    * @throws IllegalArgumentException if <code>c</code> does not implement
    * Number, or if it does not have a static
    * <code>valueOf(String)</code>
    * method or a single argument string constructor.
    */
    public void setNumberClass(Class c){
        if (Number.class.isAssignableFrom(c) || Comparable.class.isAssignableFrom(c)){
            myClass = c;
        } else {
            throw new IllegalArgumentException(String.valueOf(c) + " is not a " +
            "comparable subclass of Number");
        }
        method = null;
        constructor = null;
        try{
            method = c.getMethod("valueOf",new Class[]{String.class});
        } catch (NoSuchMethodException exm){
            try {
                constructor = c.getConstructor(new Class[]{String.class});
            } catch (NoSuchMethodException exc){
                throw new IllegalArgumentException("Caused by " + exc.toString());
            }
        }
        min = max = null;
    }
    
    /** Sets the largest allowed number.
    * @param max upper limit. null for none.
    * @throws ClassCastException if <code>max</code> is not <code>null</code>
    * or an instance of <code>getNumberClass</code>.
    */
    public void setUpperLimit(Comparable max){
        if(max == null){
            this.max = null;
        } else {
            if(!(myClass.isInstance(max))){
                throw new ClassCastException("max (" + max + ") is not an " +
                "instance of " + myClass);
            }
            this.max = max;
        }
    }
    
    /** Returns the largest allowed number.
    * @return upper limit. null for none.
    */
    public Comparable getUpperLimit(){
        return max;
    }
    
    /** Sets the smallest allowed number.
    * @param min lower limit. null for none.
    * @throws ClassCastException if <code>min</code> is not <code>null</code>
    * or an instance of <code>getNumberClass</code>.
    */
    public void setLowerLimit(Comparable min){
        if(min == null){
            this.min = null;
        } else {
            if(!(myClass.isInstance(min))){
                throw new ClassCastException("min (" + min + ") is not an " +
                "instance of " + myClass);
            }
            this.min = min;
        }
    }
    
    /** Explains why the last call to {@link #verify} returned false. If the
    * last call to <code>verify</code> returned false, this method will return
    * <ul>
    * <li><code>null</code> - if the input was out of range
    * <li>a NumberFormatException  - if the input could not be parsed as a
    * number of the correct type
    * <li>or another Exception
    * </ul>
    * @return an Exception or <code>null</code>
    */
    public Exception explain(){
        return ex;
    }
    
    /** Returns the smallest allowed number.
    * @return lower limit. null for none.
    */
    public Comparable getLowerLimit(){
        return min;
    }
    
    /**
    * @throws IllegalArgumentException if <code>input</code> is not a
    * JTextComponent  
    */
    public boolean verify(JComponent input){
        if(!(input instanceof JTextComponent)){
            throw new IllegalArgumentException(input.toString() + " is not a " +
            "JTextComponent.");
        }
        boolean result = false;
        ex = null;
        Object[] in = new Object[]{((JTextComponent) input).getText()};
        try{
            Object num = (method == null)
                ? constructor.newInstance(in)
                : method.invoke(null, in);
            result = 
                ( min == null || min.compareTo(num) <= 0 ) &&
                ( max == null || max.compareTo(num) >= 0 );
        } catch(Exception failed){
            ex = failed;
        }
        return result;
    }
}
