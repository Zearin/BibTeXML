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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/** An input verifier that checks if a text component's text can be interpreted
* as a value of a particular class and optionally if it falls into an allowed
* range of values of that class.<p>
* Here is some sample code
* <pre>
static GeneralInputVerifier giv;
static JTextComponent text = new javax.swing.JTextField();

private static void test(String s){
    text.setText(s);
    System.out.println(s + ", " + giv.verify(text) + ", " + giv.explain());
}

public static void main(String[] argv) throws Exception{
    System.out.println("Double, > -1, < 10");
    giv = new GeneralInputVerifier(Double.class, new Double(-1), new Double(10));
    test("-2");
    test("-1");
    test("5");
    test("10");
    test("11");
    test("x12");
    
    System.out.println();
    System.out.println("Date, yyyy-MM-dd, > 2007-02-15" );
    java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
    giv = new GeneralInputVerifier(java.util.Date.class,
    java.text.DateFormat.class.getMethod("parse", new Class[]{String.class}),
    format);
    giv.setLowerLimit(format.parse("2007-02-15"));
    test("2007-08-15");
    test("200a-08-15");
    test("2005-08-15");
}
</pre>
* The output is
* <pre>
Double, > -1, < 10
-2, false, null
-1, true, null
5, true, null
10, true, null
11, false, null
x12, false, java.lang.NumberFormatException: For input string: "x12"

Date, yyyy-MM-dd, > 2007-02-15
2007-08-15, true, null
200a-08-15, false, java.text.ParseException: Unparseable date: "200a-08-15"
2005-08-15, false, null
</pre>
*
* @version $Revision$ ($Date$)
* @author Moritz Ringler
*/
public class GeneralInputVerifier extends InputVerifier implements java.io.Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4218882472153372100L;
	private Class  myClass;
    private Method method;
    private Object obj = null;
    private Constructor constructor;
    private Comparable min = null;
    private Comparable max = null;
    private transient Throwable ex = null;
    
    /** Constructs a new GeneralInputVerifier for the given class and the given 
    * limits. 
    * @param c the class that input will be interpreted as 
    * @param min lower limit. null for none.
    * @param max upper limit. null for none.
    * @throws IllegalArgumentException if <code>c</code> does not have a 
    * public static <code>valueOf(String)</code> method or a 
    * public constructor with a single string argument.
    * @throws ClassCastException if <code>min</code> and <code>max</code> are
    * neither <code>null</code> nor instances of <code>c</code>. 
    */
    public GeneralInputVerifier(Class c, Comparable min, Comparable max){
        setInputClass(c);
        setUpperLimit(max);
        setLowerLimit(min);
    }
    
    /** Constructs a new GeneralInputVerifier for the full range of values
    * in the specified class. 
    * @param c the class that input will be interpreted as 
    * @throws IllegalArgumentException if <code>c</code> does not have a
    * public static
    * <code>valueOf(String)</code> method or a public constructor with
    * a single String argument
    */
    public GeneralInputVerifier(Class c){
        setInputClass(c);
    }
    
    /** Creates a new GeneralInputVerifier for the specified class
    * that uses the specified static method to construct a value from a string. 
    * @param c the class that input will be interpreted as
    * @param m a public static method that accepts a single
    * string argument and whose return type can be assigned to <code>c</code> 
    * @throws IllegalArgumentException if <code>method</code>
    * is not a public static method that accepts a single
    * string argument and has a return type of <code>c</code> 
    */
    public GeneralInputVerifier(Class c, Method m){
        setInputClass(c, m);
    }
    
    /** Creates a new GeneralInputVerifier for the specified class
    * that uses the specified method and object
    * to construct a value from a string. This will
    * automatically reset the allowed range to the full range of values 
    * in this class.
    * @param c the class that input will be interpreted as
    * @param m a method that accepts a single
    * string argument and whose return type can be assigned to <code>c</code> 
    * @param o the object to invoke the method on, or <code>null</code>
    * if <code>m</code> is static
    * @throws IllegalArgumentException if <code>method</code>
    * is not a public method of <code>o</code> that accepts a single
    * string argument and has a return type of <code>c</code> 
    */
    public GeneralInputVerifier(Class c, Method m, Object o){
        setInputClass(c, m, o);
    }
    
    /** Returns the class against which input will be checked.
    * @see #setInputClass
    */
    public Class getInputClass(){
        return myClass;
    }
    
    /** Sets the class against which input will be checked. This will
    * automatically reset the allowed range to the full range of values 
    * in this class.
    * @param c the class that input will be interpreted as 
    * @throws IllegalArgumentException if <code>c</code> does not have a public
    * static
    * <code>valueOf(String)</code> method that returns a value of <code>c</code>
    * or a public constructor with a single string argument.
    */
    public void setInputClass(Class c){
        method = null;
        constructor = null;
        final Class[] singleString = new Class[]{String.class};
        try{
            setInputClass(c, c.getMethod("valueOf", singleString));
        } catch (Exception exm){
            try {
                constructor = c.getConstructor(singleString);
                if(!Modifier.isPublic(constructor.getModifiers())){
                    throw new IllegalArgumentException(
                    "Constructor is not public.");
                }
                myClass = c;
                min = max = null;
            } catch (NoSuchMethodException exc){
                throw new IllegalArgumentException("Caused by " + exc.toString());
            }
        }
    }
    
    /** Sets the class against which input will be checked and the 
    * static method used to construct values of this class from a string.
    * This will reset the allowed range to the full range of values 
    * in this class.
    * @param c the class that input will be interpreted as
    * @param m a static method that accepts a single
    * string argument and whose return type can be assigned to <code>c</code> 
    * @throws IllegalArgumentException if <code>method</code>
    * is not a public static method that accepts a single
    * string argument and has a return type of <code>c</code> 
    */
    public void setInputClass(Class c, Method m){
        setInputClass(c, m, null);
    }
    
    /** Sets the class against which input will be checked and the 
    * method and object used
    * to construct values of this class from a string. This will
    * automatically reset the allowed range to the full range of values 
    * in this class.
    * @param c the class that input will be interpreted as
    * @param m a method that accepts a single
    * string argument and whose return type can be assigned to <code>c</code> 
    * @param o the object to invoke the method on, or <code>null</code>
    * if <code>m</code> is static
    * @throws IllegalArgumentException if <code>method</code>
    * is not a public method of <code>o</code> that accepts a single
    * string argument and has a return type of <code>c</code> 
    */
    public void setInputClass(Class c, Method m, Object o){
        /* check return type */
        if(!c.isAssignableFrom(m.getReturnType())){
            throw new IllegalArgumentException(
            c + " is not assignable from the return type of " + m);
        }
        
        /* check parameter types */
        Class[] params = m.getParameterTypes();
        if(params.length != 1 || !params[0].equals(String.class)){
            throw new IllegalArgumentException(
            m + " does not have the correct parameter types.");
        }
        
        /* check modifiers */
        final int modifiers = m.getModifiers();
        
        /* check public access */
        if(!Modifier.isPublic(modifiers)){
            throw new IllegalArgumentException(
            m + " is not public.");
        }
        
        if(o == null){
            /* check whether m is static */
            if(!Modifier.isStatic(modifiers)){
                throw new IllegalArgumentException(
                m + " is not static.");
            }
            
        } else {
            /* check whether m can be invoked on o */
            if(!m.getDeclaringClass().isInstance(o)){
                throw new IllegalArgumentException(
                m + " cannot be invoked on " + o);
            }
        }
        myClass = c;
        method = m;
        obj = o;
    }
    
    /** Sets the largest allowed value.
    * @param max upper limit. null for none.
    * @throws ClassCastException if <code>max</code> is neither <code>null</code>
    * nor an instance of <code>getInputClass</code>.
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
    
    /** Returns the largest allowed value.
    * @return upper limit. null for none.
    */
    public Comparable getUpperLimit(){
        return max;
    }
    
    /** Sets the smallest allowed value.
    * @param min lower limit. null for none.
    * @throws ClassCastException if <code>min</code> is neither <code>null</code>
    * nor an instance of <code>getInputClass</code>.
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
    
    /** Explains why the last call to {@link #verify(String) verify}
    * returned false. This method will return
    * any exception thrown within the {@link #verify(String) verify} methods.
    * @return a throwable or <code>null</code>
    */
    public Throwable explain(){
        return ex;
    }
    
    /** Returns the smallest allowed value.
    * @return lower limit. null for none.
    */
    public Comparable getLowerLimit(){
        return min;
    }
    
    /**
    * Verifies the current text of a {@link javax.swing.text.JTextComponent}.
    * @param c a swing text component
    * @return whether the current text of <code>c</code> is legal input
    * @throws ClassCastException if <code>c</code> is not a
    * JTextComponent  
    */
    public boolean verify(JComponent c){
        return verify( ((JTextComponent) c).getText() );
    }
    
    /**
    * Verifies the specified text.
    * @param text the input to verify
    * @return whether <code>text</code> is legal input.
    */
    public boolean verify(String text){
        boolean result = false;
        ex = null;
        final Object[] in = new Object[]{text};
        try{
            Object value = (method == null)
                ? constructor.newInstance(in)
                : method.invoke(obj, in);
            result = 
                ( min == null || min.compareTo(value) <= 0 ) &&
                ( max == null || max.compareTo(value) >= 0 );
        } catch(InvocationTargetException failed){
            ex = failed.getCause();
        } catch (Exception failed){
            ex = failed;
        }
        return result;
    }
}
