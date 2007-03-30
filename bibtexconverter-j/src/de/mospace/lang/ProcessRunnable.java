package de.mospace.lang;

/* Mp3dings - manage mp3 meta-information
* Copyright (C) 2006 Moritz Ringler
* $Id: ProcessRunnable.java,v 1.13 2007/02/18 14:15:47 ringler Exp $
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** A runnable whose {@link #run run} method executes a system command. 
* The system command is either specified as a single string or as an
* array of string tokens.
* @see java.lang.Runtime#exec
*/
public class ProcessRunnable implements RunnableQueue.Job{
    private String command;
    private String cmdarray[];
    private File dir = null;
    private String[] envp;
    
    /** Where the error stream of this Runnable's system command
    * will be redirected.
    * By default this is a {@link NullStream}. */ 
    protected Output errp = new StreamOutput(NullStream.getInstance());
    
    /** Where the output stream of this Runnable's system command
    * will be redirected.
    * By default this is a {@link NullStream}. */ 
    protected Output outp = errp;
    
    /** Where this Runnable's system command will read its standard input from.
    * By default this is <code>null</code>. */ 
    protected Input inpt;
    
    private transient Process process = null;
    private transient int exitValue = 0;
    private ExceptionHandler exceptionHandler;
    private static ThreadGroup iothreads = new ThreadGroup("ProcessRunableIO");

    /** Constructs a new ProcessRunnable whose {@link #run} method will execute
    * the specified command.
    * @param command the system command to execute 
    */
    public ProcessRunnable(String command){
        this.command = command;
    }

    /** Constructs a new ProcessRunnable whose {@link #run} method will execute
    * the command made up of the specified tokens.
    * @param cmdarray the tokens that together form the command line to execute
    */
    public ProcessRunnable(String[] cmdarray){
        setCommand(cmdarray);
    }

    /** Throws an IllegalStateException when this process runnable is
    * currently running.
    * @throws IllegalStateException when this process runnable is
    * currently running.
    */
    private void checkRunning()
    throws IllegalStateException{
        if(isRunning()){
            throw new IllegalStateException("Process is running.");
        }
    }

    /** Sets the command that will be executed when {@link #run} is called.
    * @param cmd the system command to execute 
    * @throws IllegalStateException when this process runnable is
    * currently running.
    */
    public void setCommand(String cmd)
            throws IllegalStateException{
        checkRunning();
        cmdarray = null;
        command = cmd;
    }

    /** Returns the command that will be executed
    * when {@link #run} is called.
    * @return the system command to execute or <code>null</code>
    * if this process runnable's command has been specified as an array.
    */
    public String getCommand(){
        return command;
    }

    /** Sets the command line that will be executed
    * when {@link #run} is called.
    * @param cmdarray the tokens that together form the command line to
    * execute
    * @throws IllegalStateException when this process runnable is
    * currently running.
    */
    public void setCommand(String[] cmdarray)
            throws IllegalStateException{
        checkRunning();
        command = null;
        this.cmdarray = new String[cmdarray.length];
        System.arraycopy(cmdarray, 0, this.cmdarray, 0, cmdarray.length);
    }

    /** Returns a copy of the command line that will be executed
    * when {@link #run} is called.
    * @return the command line to execute or <code>null</code>
    * if this process runnable's command has been specified as a single string.
    */
    public String[] getCommandArray(){
        String[] cmds = new String[cmdarray.length];
        System.arraycopy(cmdarray,0,cmds,0,cmdarray.length);
        return cmds;
    }

    /** Returns the command that will be executed
    * when {@link #run} is called, independently of whether this command
    * has been specified as a single string or as an array of string tokens.
    * @return the system command to execute
    */
    public String getCommandAsString(){
        String result = getCommand();
        if(result == null){
            String[] ca = getCommandArray();
            if(ca == null){
                result = "null";
            } else {
                result = Array.join(ca,' ');
            }
        }
        return result;
    }

    /** Redirects output of this process runnable's system command to
    * the specified stream.
    * When <code>null</code> is passed output from the external process
    * will be read and discarded (the default). The specified
    * output stream will not be
    * closed by this class.
    * @param o the output stream the program should write its output to
    * @throws IllegalStateException when this process runnable is
    * currently running.
    */
    public void redirectOutputTo(OutputStream o)
            throws IllegalStateException{
        checkRunning();
        outp = new StreamOutput(o);
    }

    /** Redirects output of this process runnable's system command to
    * the specified file. The file will be opened for writing and 
    * closed when the system commands starts and stops
    * executing, respectively.
    * @param f the file the program should write its output to
    * @throws IllegalStateException when this process runnable is
    * currently running.
    */
    public void redirectOutputTo(File f)
            throws IllegalStateException{
        checkRunning();
        outp = new FileOutput(f);
    }

    /** Redirects error output of this process runnable's system command to
    * the specified stream.
    * When <code>null</code> is passed output from the external process
    * will be read and discarded (the default). The specified
    * output stream will not be
    * closed by this class.
    * @param o the output stream the program should write its output to
    * @throws IllegalStateException when this process runnable is
    * currently running.
    **/
    public void redirectErrorsTo(OutputStream o)
            throws IllegalStateException{
        checkRunning();
        errp = new StreamOutput(o);
    }

    /** Redirects error output of this process runnable's system command to
    * the specified file. The file will be opened for writing and 
    * closed when the system commands starts and stops
    * executing, respectively.
    * @param f the file the program should write its output to
    * @throws IllegalStateException when this process runnable is
    * currently running.
    **/
    public void redirectErrorsTo(File f)
            throws IllegalStateException{
        checkRunning();
        errp = new FileOutput(f);
    }

    /** Sets the environment for this process runnable's system command.
    * @param envp the environment for this process runnable's system command
    * specified as an array of <code>"key=value"</code> pairs.
    * @throws IllegalStateException when this process runnable is
    * currently running.
    * @see java.lang.Runtime#exec(String[] cmd,String[] envp, File dir)
    * @see #getEnvironment
    **/
    public void setEnvironment(String[] envp)
            throws IllegalStateException{
        checkRunning();
        this.envp = new String[envp.length];
        System.arraycopy(envp,0,this.envp,0,envp.length);
    }

    /** Returns the environment for this process runnable's system command.
    * @return the environment for this process runnable's system command
    * as an array of <code>"key=value"</code> pairs.
    * @see java.lang.Runtime#exec(String[] cmd,String[] envp, File dir)
    * @see #setEnvironment
    **/
    public String[] getEnvironment(){
        String[] env = null;
        if(envp != null){
            env = new String[envp.length];
            System.arraycopy(envp,0,env,0,envp.length);
            return env;
        }
        return env;
    }

    /** Sets the working directory for this process runnable's system command.
    * @param dir the working directory for this process runnable's system 
    * command
    * @throws IllegalStateException when this process runnable is
    * currently running.
    * @see java.lang.Runtime#exec(String[] cmd,String[] envp, File dir)
    * @see #getWorkingDirectory
    */
    public void setWorkingDirectory(File dir)
            throws IllegalStateException{
        checkRunning();
        this.dir = dir;
    }

    /** Returns the working directory for this process runnable's system command.
    * @return the working directory for this process runnable's system 
    * command
    * @see #getWorkingDirectory
    */
    public File getWorkingDirectory(){
        return dir;
    }

    /** Executes this process runnable's system command. If we are already
    * running then we first wait until the process spawned on the last
    * invocation terminates.*/
    public void run(){
        try{
            if(isRunning()){
                waitFor();
            }
            process = null;
            if (cmdarray != null && cmdarray.length != 0){
                process = Runtime.getRuntime().exec(cmdarray, envp, dir);
            } else if (command != null){
                process = Runtime.getRuntime().exec(command, envp, dir);
            } else {
                return;
            }
            if (process == null){
                return;
            }
            final Input pout = new StreamInput(process.getInputStream());
            final Input perr = new StreamInput(process.getErrorStream());
            final Output pin = new StreamOutput(
                new BufferedOutputStream(process.getOutputStream()));
            /* redirect output stream of process to out */
            (new PipeThread(this, pout, outp)).start();
            /* redirect error stream of process to err */
            (new PipeThread(this, perr, errp)).start();
            /* redirect input to input stream of process */
            if(inpt != null){
                (new PipeThread(this, inpt, pin)).start();
            }

            /* monitor process state */
            exitValue = waitFor();
            /* this will make isRunning() return false */
            process = null;
        } catch (Exception ex){
            ex.printStackTrace();
            stop();
            if(exceptionHandler == null){
                throw new RuntimeException(ex);
            } else {
                exceptionHandler.handleException(ex);
            }
        }
    }

    /** Registers an exception handler to deal with exceptions
    * thrown within the {@link #run} method. By default any exceptions
    * thrown within run will be re-thrown as RuntimeExceptions.
    * @param xh an exception handler
    */
    public void registerExceptionHandler(ExceptionHandler xh){
        exceptionHandler = xh;
    }

    /** Returns the exception handler that deals with exceptions
    * thrown within the {@link #run} method.
    * @return the exception handler registered with this ProcessRunnable if any 
    * @see #registerExceptionHandler
    */
    protected ExceptionHandler getExceptionHandler(){
        return exceptionHandler;
    }

    /** Kills the system process spawned by the {@link #run} method if
    * this process runnable is currently running. */    
    public void stop(){
        if (isRunning()){
            process.destroy();
            process = null;
        }
    }

    /** Waits for the system process spawned by the {@link #run} method to
    * terminate if this process runnable is currently running. In this case
    * the exit value of the currently running process is returned. Otherwise
    * this method has the same effect as {@link #getExitStatus}.
    * @return the exit value of the currently running process
    * @throws InterruptedException if the system process is interrupted
    */    
    public int waitFor() throws InterruptedException{
        if (isRunning()){
            return process.waitFor();
        } else {
            return exitValue;
        }
    }

    /** Returns the exit status of the system process that returned last.
    * @return the exit status of the system process that returned last or zero
    * if this runnable has never run.
    */   
    public int getExitStatus(){
        return exitValue;
    }

    /** Returns whether this process runnable is currently running.
    * @return whether this process runnable is currently running
    */
    public boolean isRunning(){
        return (process != null);
    }

    /** Makes this process runnable's system command read input from the
    * specified file. The file will be opened for reading and 
    * closed when the system commands starts and stops
    * executing, respectively.
    * @param f the file to read input from
    */    
    public void readInputFrom(File f){
        inpt = new FileInput(f);
    }

    /** Makes this process runnable's system command read input from the
    * specified stream.
    * @param i the stream to read input from
    */   
    public void readInputFrom(InputStream i){
        inpt = new StreamInput(i);
    }

    /** Returns whether this process runnable currently has a non-null input.
    * @return whether this process runnable has a non-null input
    * @see #readInputFrom(File)
    * @see #readInputFrom(InputStream)
    */
    public boolean hasInput(){
        return inpt != null;
    }

    /** Causes this process runnable to not provide any standard input
    * to its system command. */
    public void clearInput(){
        inpt = null;
    }

    /** A thread that writes
    * bytes from its input to its output as long as
    * the associated ProcessRunnable is running. All pipe threads belong to the
    * same thread group named ProcessRunableIO.
    */ 
    protected static class PipeThread extends Thread{
        /** The input that this PipeThread reads from. */
        protected Input inp;
        
        /** The output that this PipeThread writes to. */
        protected Output outp;
        
        byte[] myBuff = new byte[1024];
        private final ProcessRunnable owner;
        
        /** The common ThreadGroup of all PipeThreads. */
        private static ThreadGroup iothreads = new ThreadGroup("ProcessRunableIO");

        /** Creates a new PipeThread with the specified owner, input, and
        * output, and an auto-generated thread name.
        * @param the ProcessRunnable whose input and output the new PipeThread
        * will handle
        * @param read the input that the new PipeThread reads from
        * @param writep the output that the new PipeThread writes to
        **/
        PipeThread(ProcessRunnable owner, Input read,
        Output writep){
            this(owner, read, writep, null);
        }
        
        /** Creates a new PipeThread with the specified owner, input, 
        * output, and thread name.
        * @param the ProcessRunnable whose input and output the new PipeThread
        * will handle
        * @param read the input that the new PipeThread reads from
        * @param writep the output that the new PipeThread writes to
        * @param id the thread name for the new PipeThread. If it is <code>
        * null</code> or empty than a thread name is auto-generated. 
        **/
        PipeThread(ProcessRunnable owner, Input read,
        Output writep, String id){
            super(iothreads, (id==null || id.equals(""))
            ? "PipeThread: " + owner.getCommandAsString() + " " +
            read.toString() + " " +
            writep.toString() + " (Created " +
            System.currentTimeMillis() +")"
            : id);
            outp = writep;
            inp = read;
            this.owner = owner;
        };
        
        /** Opens this pipe thread's input and output and writes any
        * bytes that become available on the input to the output. 
        * When it encounters an End Of File on the input or when the
        * process runnable associated with this PipeThread stops running
        * the close methods of input and output will be called.
        */
        public void run(){
            InputStream input = null;
            OutputStream output = null;
            try {
                input = inp.openInputStream();
                if(input == null){ /* treat null as empty input */
                    return;
                }
                output = outp.openOutputStream();
                
                for (int i = 0; i > -1; i = input.read(myBuff)) {
                    synchronized (output){
                        output.write(myBuff,0,i);
                        output.flush();
                        output.notifyAll();
                    }
                    if(!owner.isRunning()){
                        break;
                    }
                }
                output.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try{
                    if(output != null){
                        outp.closeOutputStream(output);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try{
                    if(input != null){
                        inp.closeInputStream(input);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /** A common interface for various kinds of byte sources. */
    protected static interface Input{
        
        /** Opens and/or returns this byte source as an input stream.
        * @return the input stream for this byte source
        */
        public InputStream openInputStream() throws IOException;
        
        /** Signals that no more bytes will be read from the specified
        * input stream. Depending on the kind of input the input stream
        * may or may not be closed by this method.
        * @param stream an input stream obtained from a previous call to
        * {@link #openInputStream}.
        */
        public void closeInputStream(InputStream stream) throws IOException;
    }

    /** Wraps a file as a {@link Input}. */
    protected final static class FileInput implements Input{
        private final File f;

        /** Creates a new {@link Input} object from the specified file. **/  
        public FileInput(File f){
            this.f = f;
        }

        /** Opens the file specified in the constructor for reading. 
        * @return a new {@link java.io.FileInputStream} object
        */
        public InputStream openInputStream() throws IOException{
            return new FileInputStream(f);
        }

        /** Closes the specified stream.
        * @param stream an input stream obtained from a previous call to
        * {@link #openInputStream}.
        */
        public void closeInputStream(InputStream stream) throws IOException{
            stream.close();
        }

        /** Returns a string representation of this object. 
         * @return  a string representation of this object. */
        public String toString(){
            return (f==null)? "null" : "<"+f.getPath();
        }
    }

    /** Wraps an input stream as a {@link Input}. */
    protected final static class StreamInput implements Input{
        private InputStream inp;

        /** Creates a new {@link Input} object from the specified stream. 
        * @param stream an InputStream or <code>null</code> for empty input. **/
        public StreamInput(InputStream stream){
            inp = stream;
        }

        /** Returns the input stream specified in the constructor.
        * Clients should handle <code>null</code> gracefully
        *  treating it as empty input.
        * @return an input stream or <code>null</code>
        */
        public InputStream openInputStream(){
            return inp;
        }

        /** Does nothing. **/
        public void closeInputStream(InputStream stream){
        }

        /** Returns a string representation of this object. 
         * @return  a string representation of this object. */
        public String toString(){
            return "<"+String.valueOf(inp);
        }
    }

    /** A common interface for various kinds of byte sinks. */
    protected static interface Output{
        
        /** Opens and/or returns this byte sink as an output stream.
        * @return the output stream for this byte sink
        */
        public OutputStream openOutputStream() throws IOException;
        
        /** Signals that no more bytes will be written to the specified
        * output stream. Depending on the kind of output the output stream
        * may or may not be closed by this method.
        * @param out an output stream obtained from a previous call to
        * {@link #openOutputStream}.
        */
        public void closeOutputStream(OutputStream out) throws IOException;
    }

    /** Wraps a file as a {@link Output}. */
    protected static class FileOutput implements Output{
        private File f;

        /** Creates a new {@link Output} object from the specified file. **/ 
        public FileOutput(File f){
            this.f = f;
        }

        /** Opens the file specified in the constructor for writing. 
        * @return a new {@link java.io.FileOutputStream} object
        */
        public OutputStream openOutputStream() throws IOException{
            return new FileOutputStream(f);
        }

        /** Closes the specified stream.
        * @param out an output stream obtained from a previous call to
        * {@link #openOutputStream}.
        */
        public void closeOutputStream(OutputStream out) throws IOException{
            out.close();
        }

        /** Returns a string representation of this object. 
         * @return  a string representation of this object. */
        public String toString(){
            return (f==null)? "null" : ">"+f.getPath();
        }
    }

    /** Wraps an input stream as a {@link Input}. */
    protected static class StreamOutput implements Output{
        private OutputStream s;

        /** Creates a new {@link Output} object from the specified stream. 
        * @param o an OutputStream or <code>null</code> for a
        * {@link NullStream}. **/
        public StreamOutput(OutputStream o){
            s = (o==null)? NullStream.getInstance(): o;
        }

        /** Returns the output stream for this StreamOutput.
        * @return a non-null output stream
        */
        public OutputStream openOutputStream(){
            return s;
        }

        /** Does nothing */
        public void closeOutputStream(OutputStream out){
        }

        /** Returns a string representation of this object. 
         * @return  a string representation of this object. */
        public String toString(){
            return ">"+String.valueOf(s);
        }
    }

}