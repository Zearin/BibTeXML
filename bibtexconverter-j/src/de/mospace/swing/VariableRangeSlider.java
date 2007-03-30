/* $Id: VariableRangeSlider.java,v 1.7 2006/03/14 11:49:18 ringler Exp $
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
package de.mospace.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** A JSlider whose range and
 *  central value can be modified by the user.
 *
 *  @version $Revision: 1.7 $ ($Date: 2006/03/14 11:49:18 $)
 *  @author Moritz Ringler
 **/
public class VariableRangeSlider extends JPanel {

    /** A constant holding the maximum value allowed for
     *  logrange in {@link #setLogRange(int) setLogRange(int)}.
     **/
    public final static int    MAX_LOG_RANGE     =
             (int) (Math.log(Double.MAX_VALUE)/Math.log(10.) - 1.);

    /** A constant holding the minimum value allowed for
     *  logrange in {@link #setLogRange(int) setLogRange(int)}.
     **/
    public final static int    MIN_LOG_RANGE     = - MAX_LOG_RANGE;

    /** A constant holding the largest positive finite
     *  value the slider can assume. **/

    public final static double MAX_VALUE = Double.MAX_VALUE/10.;
    /** A constant holding the smallest positive nonzero value
     *  the slider can assume.
     **/
    public final static double MIN_VALUE = Double.MIN_VALUE*10.;

    /** The NumberFormat used to output the slider value.
     **/
    public final static DecimalFormat NUMBER_FORMAT =
            new DecimalFormat("##0.#E0",	new DecimalFormatSymbols(Locale.US));
    /** The slider will have 2 halfgridsize + 1 steps.
     **/
    private int halfgridsize = 100;

    /** The internal JSlider of the VariableRangeSlider.
     **/
    protected JSlider slider = new JSlider(JSlider.VERTICAL, -halfgridsize,
            halfgridsize,0);

    /** The JTextField used to set and print the value of the slider.
     **/
    protected JFormattedTextField value =
            new JFormattedTextField(NUMBER_FORMAT);

    /** The JSpinner used to set the power of ten that corresponds to
     *  half the slider length.
     **/
    protected JSpinner range = new JSpinner(new SpinnerNumberModel(1,
            MIN_LOG_RANGE, MAX_LOG_RANGE, 1));

    /** The List that holds all registered changeListeners. */
    private List changeListeners = new ArrayList();

        /** Tells the slider change listener whether the change is caused
        by a slider reset. **/
        private boolean isReset = false;
    //It is necessary to use this timer because otherwise the user interface
    //isn't properly updated when the slider is reset after dragging it with
    //the mouse. This is ugly. Change it if you find a way.
    /** Timer that resets the slider 1
     *  milliseconds after it has been released.
     **/
    private Timer resetSlider = new Timer(1, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            isReset = true;
            slider.setValueIsAdjusting(true);
        slider.setValue(0);
        slider.setValueIsAdjusting(false);
        isReset = false;
            resetSlider.stop();
        }
    });
    private Dimension preferredSize;


    /**
     * Constructs a new slider with central value 0,
     * range -10 to 10 and gridsize 201.
     */
    public VariableRangeSlider() {
        value.setValue((Number) new Double(0.0));
        initComponents();
        configureUI();
    }

    /**
     * Constructs a new slider with the given central value,
     * and range &#177;10<sup>logrange</sup> and gridsize 101.
     */
    public VariableRangeSlider(double value, int logrange){
        setLogRange(logrange);
        setValue(value);
        initComponents();
        configureUI();
    }

    /** Coordinates the child components and determines when a ChangeEvent
     *  is fired by adding appropriate EventListeners.
     */
    private void initComponents(){

        //text field
        value.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if ("value".equals(e.getPropertyName())) {
                    Number value = (Number) e.getNewValue();
                    if (slider != null && value != null) {
                        slider.setValue(0);
                        fireStateChanged();
                    }
                }
            }
        });
        value.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                0),"check");
        value.getActionMap().put("check", new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                if (!value.isEditValid()) {
                    value.selectAll();
                } else try {
                    value.commitEdit();
                } catch (ParseException exc) { }
            }
        });

        //slider
        slider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                //Do nothing when the change is due to
                //a re-centering (reset) of the JSlider
            //MIGHT LEAD TO WRONG BEHAVIOUR!
                if(isReset){
                return;
                }
                // Slider is moving
                // -> update the *text* of the formatted text field
                //    and make VariableRangeSlider fire a ChangeEvent
            //if (slider.getValueIsAdjusting() && !isReset){
                if (slider.getValueIsAdjusting()){
                    value.setText(getFormattedValue());
                    fireStateChanged();
                }
                // Value has been changed with the slider
                // and slider has been released.
                // -> update the *value* of the formatted text field
                //    and reset slider to central position
                // Here the the formatted text field will make
                // the VariableRangeSlider fire a ChangeEvent after
                // the slider has been reset to zero.
                else if(slider.getValue() != 0){
                    setTextFieldFromSlider();
                }
                // Slider rests and is in central position
                // -> do nothing
                //else if (getValue() == 0){
                //	return;
                //}
            }
        });
    }

    /** Sets up the user interface of the slider.
     **/
    private void configureUI(){
        //JTextField
        value.setColumns(5);
        value.setHorizontalAlignment(JFormattedTextField.RIGHT);

        //JSpinner
        range.setPreferredSize(new Dimension(40,20));
        ((JSpinner.DefaultEditor) range.getEditor()).getTextField().
            setHorizontalAlignment(JFormattedTextField.LEFT);
        JLabel spinnerLabel = new JLabel("\u00b11e");
        JPanel spinnerPanel = new JPanel(new BorderLayout());
        spinnerPanel.add(spinnerLabel, BorderLayout.WEST);
        spinnerPanel.add(range, BorderLayout.CENTER);

        //this
        setLayout(new BorderLayout());
        add(slider, BorderLayout.CENTER);
        add(value, BorderLayout.NORTH);
        add(spinnerPanel, BorderLayout.SOUTH);

        //calculate the size of this component;
        int[] x = new int[3];
        x[0] = value.getPreferredSize().width;
        x[1] = slider.getPreferredSize().width;
        x[2] = range.getPreferredSize().width +
               spinnerLabel.getPreferredSize().width;
        int xx = (x[0] > x[1]) ? x[0] : x[1];
        if(x[2] > xx){
            xx = x[2];
        }
        int y = value.getPreferredSize().height +
                slider.getPreferredSize().height +
                range.getPreferredSize().height;
        preferredSize = new Dimension(xx,y);
    }

    /** Reads the internal slider value and transfers it to the text field.
     **/
    private void setTextFieldFromSlider(){
        double r  = Math.pow(10, (double) getLogRange());
        double o = slider.getValue()*1./halfgridsize * r;
        try{
            setValue(((Number) value.getValue()).doubleValue()+o);
        } catch (IllegalArgumentException ex){
            slider.setValue(0);
        }
    }

    /** Sets the value of the slider to d and makes
     * it resume a central position.
     * @param d a legal double value
     * @throws java.lang.IllegalArgumentException when d is out of range
     * @see #MAX_VALUE
     * @see #MIN_VALUE
     **/
     public void setValue(double d){
        double r = (d>0)?d:-d;
        if(r>MAX_VALUE/10 || (r<MIN_VALUE*10 && r != 0)){
            throw new IllegalArgumentException(d + "is out of range!");
        }
        value.setValue((Number) new Double(d));
        resetSlider.start();
    }

    /** Gets the current value of the slider.
     *  @return the value of the slider
     **/
    public double getValue(){
        if (!slider.getValueIsAdjusting()){
            return ((Number) value.getValue()).doubleValue();
        } else {
            double r  = Math.pow(10, (double) getLogRange());
            double o = slider.getValue()*1./halfgridsize * r;
            return ((Number) value.getValue()).doubleValue() + o;
        }
    }

    /** Gets the current value of the slider as a formatted String.
     *  @return the value of the slider
     *  @see #NUMBER_FORMAT
     **/
     public String getFormattedValue(){
        return NUMBER_FORMAT.format(getValue());
     }


    /** Sets the number of steps used for the slider.
     *  @param i will automatically be converted to an odd integer >= 3.
     **/
    public void setGridsize(int i){
        //ensure gridsize makes sense;
        if (i < 0){
            i = - i;
        }
        if (i < 3){
            i = 3;
        }
        halfgridsize = i/2; //integer division!!
    }

    /** Gets the number of steps used for the slider.
     *  @return the number of steps
     **/
    public int getGridsize(){
        return halfgridsize*2+1;
    }

    /**	Sets the power of ten that corresponds to half the slider length.
     *  @param lr log10 of the desired range
     **/
    public void setLogRange(int lr){
        if(lr>MAX_LOG_RANGE || lr<MIN_LOG_RANGE){
            throw new IllegalArgumentException(lr + " is out of range!");
        } else {
            range.setValue(new Integer(lr));
        }
    }

    /**	Gets the power of ten that corresponds to half the slider length.
     *  @return log10 of the current range
     **/
    public int getLogRange(){
        return ((Integer) range.getValue()).intValue();
    }

    /**
     * Adds a <code>ChangeListener</code> to the slider.
     * @param l the listener to be added
     */
    public synchronized void addChangeListener(ChangeListener l){
        changeListeners.add( l );
    }

    /**
     * Removes a ChangeListener from the slider.
     * @param l the listener to be removed
     */
    public synchronized void removeChangeListener(ChangeListener l ){
        changeListeners.remove( l );
    }

    /**
     * Notifies all registered ChangeListeners.
     */
    protected synchronized void fireStateChanged(){
        ChangeEvent e = new ChangeEvent(this);
        Iterator listenersIt = changeListeners.iterator();
        while(listenersIt.hasNext()){
            ((ChangeListener) listenersIt.next()).stateChanged(e);
        }
    }

    /**
     * Overrides method in superclass
     * {@link java.awt.Component#getPreferredSize() java.awt.Component}.
     */
    public Dimension getPreferredSize(){
        return preferredSize;
    }

     /**
     * Overrides method in superclass
     * {@link java.awt.Component#getMinimumSize() java.awt.Component}.
     */
    public Dimension getMinimumSize(){
        return getPreferredSize();
    }

    /**
     * Overrides method in superclass {@link java.awt.Component#preferredSize()
     * java.awt.Component}.
     * @deprecated
     * @see #getPreferredSize
     */
    public Dimension preferredSize(){
        return getPreferredSize();
    }

    /**
     * Overrides method in superclass
     * {@link java.awt.Component#minimumSize() java.awt.Component}.
     * @deprecated
     * @see #getMinimumSize
     */
    public Dimension minimumSize(){
        return getPreferredSize();
    }
}
