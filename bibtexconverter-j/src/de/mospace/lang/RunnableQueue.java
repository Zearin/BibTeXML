package de.mospace.lang;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2006 Moritz Ringler
* $Id: RunnableQueue.java,v 1.4 2007/02/18 14:15:47 ringler Exp $
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
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/** A queue for Runnables that adhere to the {@link Job} interface,
* enqueued jobs are run one after the other in the order in which they have
* been enqueued. 
* @see java.lang.Runnable
**/
public class RunnableQueue implements Runnable{
    /** A Runnable that can be enqueued to a RunnableQueue. */
    public static interface Job extends Runnable{
        /** Returns the exit status of this job. Should be zero by default and
        * before the job has run.
        * @return the exit status of this job
        */
        public int getExitStatus();
        
        /** Requests that this job stops executing if it is currently running.
        * This method will usually be called on another thread than 
        * <code>run()</code>. Implementations may choose to ignore this.
        */
        public void stop();
        
        /** Returns whether this job currently has a non-null input.
        *  Jobs that don't require any input should always return 
        * <code>true</code>.
        * @return whether this job has a non-null input
        * @see #readInputFrom(InputStream)
        */
        public boolean hasInput();
        
        /** Requests that this job reads any input it needs
        * from the specified stream. Jobs that don't require any input may
        * ignore this.
        * @param stream the stream to read input from
        */   
        public void readInputFrom(InputStream stream);
        
        /** Registers an exception handler to deal with exceptions
        * thrown within the <code>run()</code> method. By default any exceptions
        * thrown within run should be re-thrown as RuntimeExceptions.
        * @param eh an exception handler
        */
        public void registerExceptionHandler(ExceptionHandler eh);
    }
    
    private ExceptionHandler eh = new ExceptionHandler(){
        public void handleException(Throwable e){
        }
    };
    private List jobs;
    private Job job;
    private OutputStream inputSink;
    private boolean goon = true;
    private int lastExitStatus;
    private Runnable fireStateChange = new Runnable(){
        public void run(){
            fireStateChanged();
        }
    };
    private ThreadGroup tg = new ThreadGroup(
    "RunnableQueueThreadGroup"+
    this.toString()
    ){
        public void uncaughtException(Thread t, Throwable e){
            eh.handleException(e);
        }
    };
    
    /** Executes this queue. Jobs will be run one after the other in the
    * order in which they have been enqueued.
    * @throws IllegalStateException when this queue is already running
    */ 
    public void run(){
        if(isRunning()){
            throw new
                IllegalStateException("Runnable queue is already running");
        }        
        goon = true;
        job = getNextRunnable();
        /* fire events on the event dispatching thread */
        if (EventQueue.isDispatchThread()) {
            fireStateChanged();
        } else {
            try{
                SwingUtilities.invokeAndWait(fireStateChange);
            } catch (InterruptedException ex){
                System.err.println("Interrupted");
                ex.printStackTrace();
            } catch (InvocationTargetException ex){
                System.err.println("InvocationTarget");
                ex.printStackTrace();
            }
        }
        while(goon && job != null){
            jobs.remove(0);
            if(!job.hasInput()){
                PipedOutputStream pipeo =  null;
                PipedInputStream pipei = null;
                try{
                    pipeo = new PipedOutputStream();
                    pipei = new PipedInputStream(pipeo);
                    job.readInputFrom(pipei);
                    inputSink = pipeo;
                    job.run();
                } catch (IOException ex){
                    ex.printStackTrace();
                } finally {
                    inputSink = null;
                    try{
                        if(pipeo != null){
                            pipeo.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try{
                        if(pipei != null){
                            pipei.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                inputSink = null;
                job.run();
            }
            lastExitStatus = job.getExitStatus();
            job = getNextRunnable();
        }
        goon = false;
        job = null;
        jobs.clear();
        /* fire events on the event dispatching thread */
        if (EventQueue.isDispatchThread()) {
            fireStateChanged();
        } else {
            try{
                SwingUtilities.invokeAndWait(fireStateChange);
            } catch (InterruptedException ex){
                System.err.println("Interrupted");
                ex.printStackTrace();
            } catch (InvocationTargetException ex){
                System.err.println("InvocationTarget");
                ex.printStackTrace();
            }
        }
    }
    
    private Thread createThread(){
        return new Thread(
        tg,
        "RunnableQueueThread:"+this.toString()
        ){
            public void run(){
                RunnableQueue.this.run();
            }
        };
    }
    
    /** Creates a new RunnableQueue with the specified jobs.
    * @param commands an array of non-null jobs to initialize the queue with 
    */
    public RunnableQueue(Job[] commands){
        if (commands != null){
            for(int i=0; i<commands.length; i++){
                enqueue(commands[i]);
            }
        }
    }
    
    /** Creates a new RunnableQueue with no jobs. */
    public RunnableQueue(){
        this(null);
    }
    
    /** Registers an exception handler for all jobs in this queue
    * and for all threads started with this queue's {@link #start}
    * method.
    * @param xh an exception handler
    * @see Job#registerExceptionHandler
    */
    public void registerExceptionHandler(ExceptionHandler xh){
        eh = xh;
        if(jobs != null){
            synchronized(jobs) {
                Iterator i = jobs.iterator(); // Must be in synchronized block
                while (i.hasNext()){
                    ((Job) i.next()).registerExceptionHandler(xh);
                }
            }
        }
    }
    
    /** Returns the job that will next start executing.
    * @return the job that will next start executing
    * or <code>null</code> if no job is awaiting execution in the queue
    */
    public Job getNextRunnable(){
        if(jobs != null && jobs.size() > 0){
            return (Job) jobs.get(0);
        } else {
            return null;
        }
    }
    
    /** Returns the currently running job.
    * @return the currently running job from this queue or <code>null</code>
    * if no job is running. **/
    public Job getRunningRunnable(){
        return job;
    }
    
    /** Adds a new job to the end of this queue. If this queue is currently
    * running then this job will be executed once the currently running job
    * and the jobs enqueued before it have terminated. If this queue is not
    * running then this job will not be executed unless {@link #run} or
    * {@link #start} is called.
    * @param p the job to add to the queue
    */
    public void enqueue(Job p){
        if(jobs == null){
            jobs = Collections.synchronizedList(new Vector());
        }
        p.registerExceptionHandler(eh);
        jobs.add(p);
    }
    
    /** Removes the first occurrence in this queue of the
    * specified job. If this queue does not contain the element,
    * it is unchanged.
    * @param p the job to dequeue
    * @see java.util.List#remove(Object)
    * @return true if the job was in the queue, false otherwise
    **/
    public boolean dequeue(Job p){
        return jobs.remove(p);
    }
    
    /** Returns whether this queue is currently running.
    * @return whether this queue is currently running
    */ 
    public boolean isRunning(){
        return (job != null);
    }
    
    /** Starts to execute this queue in a new thread. 
    * @throws IllegalStateException when this queue is already running
    */
    public void start(){
        createThread().start();
    }
    
    /** Stops the currently running job in this queue. If there are further
    * jobs in the queue then these will be executed.
    * @see Job#stop
    */
    public void stop(){
        if (isRunning()){
            job.stop();
        }
    }
    
    /** Stops execution of this queue. The currently running job will be
    * stopped and further jobs in the queue will not be executed and
    * remain queued.
    * @see Job#stop
    */
    public void interrupt(){
        if (isRunning()){
            job.stop();
            goon = false;
        }
    }
    
    /** Removes all jobs from the queue. Does not stop a currently running job.
    */
    public void clear(){
        jobs.clear();
    }
    
    /** Forwards text input to the currently running job if it does not have
    * another source of input.
    * @param input text input for the currently running job
    * @see Job#hasInput
    */
    public void handleInput(String input) throws IOException{
        if (inputSink != null){
            inputSink.write(input.getBytes());
            inputSink.flush();
        }
    }
    
    /* Event generation and listener management */
    EventListenerList listenerList = new EventListenerList();
    ChangeEvent changeEvent = null;
    
    /** Registers a listener that will be notified whenever this runnable
    * queue starts or stops executing. A listener can differentiate the two cases
    * by calling <code>event.getSource().isRunning()</code>, this will return
    * true iff the queue has started.
    * @param l a change listener to be notified of future changes in this
    * queue's state
    */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    
    /** Requests that the specified listener no longer be notified when this
    * runnable queue starts or stops executing.
    * @param l the change listener to remove
    */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    
    /** Notifies all registered  listeners that this runnable
    * queue has started or stopped executing.
    * A listener can differentiate the two cases
    * by calling <code>event.getSource().isRunning()</code>, this will return
    * true iff the queue has started.
    */
    protected void fireStateChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
}