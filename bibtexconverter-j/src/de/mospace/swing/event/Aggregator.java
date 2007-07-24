package de.mospace.swing.event;

/* This class is part of the de.mospace.swing library.
 * Copyright (C) 2006 Moritz Ringler
 * $Id: Aggregator.java,v 1.2 2007/02/18 14:20:23 ringler Exp $
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

/**
 * When the same runnable is invoked several times through an Aggregator's
 * {@link #aggregatedInvokeLater aggregatedInvokeLater} method during a predefined delay
 * it is executed only once. This facility can be used for example to
 * reduce the frequency of costly GUI updates after changes to the
 * displayed data.
 *
 * @author Moritz Ringler
 * @version $Revision: 1.2 $ ($Date: 2007/02/18 14:20:23 $)
 */
public class Aggregator {
    private final Timer timer = new Timer(true); //run as daemon
    private final long aggDelay;
    private final boolean reset;
    private final Set currentTasks = Collections.synchronizedSet(new HashSet());

    private static class AggregatorTask extends TimerTask{
        private final Runnable job;
        private final Aggregator parent;

        public AggregatorTask(Runnable r, Aggregator parent){
            this.parent = parent;
            job = r;
        }

        public void run(){
            parent.stopAggregating(this);
            SwingUtilities.invokeLater(job);
        }

        public Runnable getRunnable(){
            return job;
        }
    }

    /** Constructs a new aggregator with the specified delay and reset behavior.
    * @param aggregationTime the period of time the aggregator waits for further
    * calls to {@link #aggregatedInvokeLater} before an invoked runnable
    * is executed.
    * @param reset wether to reset the timer waiting for further calls to
    * <code>aggregatedInvokeLater</code> when <code>aggregatedInvokeLater</code>
    * is invoked a second time with the same argument during
    * the aggregation period
    */
    public Aggregator(long aggregationTime, boolean reset){
        aggDelay = aggregationTime;
        this.reset = reset;
    }

    /** Schedules the specified runnable for execution after the aggregation
    * delay. When the same runnable is invoked another time during the
    * aggregation delay it is executed only once.
    * @param r the runnable to schedule
    */
    public void aggregatedInvokeLater(Runnable r){
        /* Checks whether this runnable is already scheduled. */
        AggregatorTask task = getTask(r);
        /* If it is not then we schedule it now and we wait whether it is
        * invoked a second time */
        if (task == null){
            startAggregating(r);
        } else if (reset && task.cancel()){
            /* it it is already scheduled and reset is true
             * then we try to stop the previously scheduled
             * task and schedule a new task executing r */
             stopAggregating(task);
             startAggregating(r);
        }
    }

    private void stopAggregating(AggregatorTask t){
        currentTasks.remove(t);
    }

    private void startAggregating(Runnable r){
        AggregatorTask task = new AggregatorTask(r, this);
        currentTasks.add(task);
        timer.schedule(task, aggDelay);
    }


    private AggregatorTask getTask(Runnable r){
        AggregatorTask result = null;
        Iterator it = currentTasks.iterator();
        while( it.hasNext() && (result == null) ){
            AggregatorTask task = (AggregatorTask) it.next();
            if(task.getRunnable().equals(r)){
                result = task;
            }
        }
        return result;
    }
}
