/* $Id: JSliderDouble.java,v 1.8 2006/03/14 11:49:18 ringler Exp $
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

import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JSlider;

/** A JSlider that accepts and returns double values.
*
* @author Moritz Ringler
* @version $Revision: 1.8 $ ($Date: 2006/03/14 11:49:18 $)
* @see javax.swing.JSlider
**/
public class JSliderDouble extends JSlider{
    private final double mmin;
    private final double sstep;

    /** Constructs a new instance of this class.
    * @param min the minimum value of the slider
    * @param max the maximum value of the slider
    * @param step the minimum stepsize of the slider
    * @param labelStep the tick spacing of the slider
    * @param initialValue the initial value of the slider
    **/
    public JSliderDouble(double min, double max, double step,
            double labelStep, double initialValue){
        super(0, (int) ((max-min)/step), (int) ((initialValue - min)/step));
        final int numSteps   = (int) ((max-min)/step);
        final int iLabelStep = (int) (labelStep/step);
        Hashtable labels = new Hashtable();
        labelStep = iLabelStep * step;
        double d = truncate(min,step);
        for(int i = 0; i <= numSteps; i+=iLabelStep){
            int ival = (int) d;
            labels.put(new Integer(i),
                new JLabel((Math.abs(d - ival) < step/10.)?
                        String.valueOf(ival):
                        String.valueOf(d)
                    ));
            d = truncate (d+labelStep, step);
        }
        super.setLabelTable(labels);
        labels = null;
        super.setMajorTickSpacing(iLabelStep);
        setPaintTicks(true);
        setPaintLabels(true);
        mmin = min;
        sstep = step;
    }

    private double truncate(double val, double step){
        double xval = val;
        double xstep = step;
        int i = 1;
        while (xstep<1){
            xval *= 10;
            i    *= 10;
            if (Math.abs(Math.round(xval) - xval) < xstep/10.){
                return ((int) Math.round(xval))/((double) i);
            }
            xstep *= 10;
        }
        return val;
    }

    /** Disabled. **/
    public void setLabelTable(Hashtable ht){
        //disabled
    }

    /** Disabled. **/
    public void setMajorTickSpacing(int i){
        //disabled
    }

    /** Returns the current value of the slider.
    * @return the current value
    **/
    public double getDoubleValue(){
        return super.getValue()*sstep + mmin;
    }
}
