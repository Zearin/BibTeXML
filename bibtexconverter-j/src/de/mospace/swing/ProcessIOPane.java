/* $Id: ProcessIOPane.java,v 1.13 2007/01/22 17:27:36 ringler Exp $
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import de.mospace.lang.ExceptionHandler;
import de.mospace.lang.ProcessRunnable;
import de.mospace.lang.RunnableQueue;
import de.mospace.swing.text.DocumentOutputStream;

/** A JComponent that provides a command line system shell. System processes
 * are started by entering commands in the input field of this IOPane.
 * Error and output streams of the system processes thus started are redirected
 * to the output area of this IOPane. The commands are executed
 * one after the other unless they end with an '&amp;' character.
 * ProcessIOPanes have a command line history but otherwise dispose of
 * only very limited built-in capabilities ('cd' and 'clear',
 * process interruption with CTRL-C, no I/O redirection). Run your
 * operating system shell (e.g. bash) inside the ProcessIOPane if you need a
 * fully-featured shell.
 *
 *  @version $Revision: 1.13 $ ($Date: 2007/01/22 17:27:36 $)
 *  @author Moritz Ringler
 **/
public class ProcessIOPane extends IOPane{
     //private boolean closeWhenDone = false;
     private File pwd;
     private final RunnableQueue myProcesses = new RunnableQueue();
     private final OutputStream iopOut = new DocumentOutputStream(getDocument());
     private static String systemShell = "";

     private final ExceptionHandler printExceptionHandler = new ExceptionHandler(){
        public void handleException(Throwable ex){
           (new PrintStream(iopOut)).print(ex.getLocalizedMessage().trim()+"\n");
        }
    };
    private final Action interruptAction = new AbstractAction(
            GLOBALS.getString("Interrupt")){
        public void actionPerformed(ActionEvent e){
            interrupt();
        }
    };

    private final Action clearAction = new AbstractAction(GLOBALS.getString("Clear")){
        public void actionPerformed(ActionEvent e){
            getOutputArea().setText("");
        }
    };

    private final Action systemShellAction = new AbstractAction(GLOBALS.getString("System shell")){
        public void actionPerformed(ActionEvent e){
            if (systemShell == null || systemShell.equals("")){
                setSystemShell(querySystemShell());
            }
            if (systemShell != null && !systemShell.equals("")){
                execute(systemShell);
            }
        }
    };

    public void setSystemShell(String command){
        systemShell = command;
        getPref().put("systemShell", (command == null)? "" : command);
    }

    public String getSystemShell(){
        return systemShell;
    }

    public String querySystemShell(){
        return (String) JOptionPane.showInputDialog(
                ProcessIOPane.this,
                "Enter system shell command",
                (systemShell == null || systemShell.equals(""))
                    ? guessSystemShell()
                    : systemShell
                );
    }

    public static String guessSystemShell(){
        String os = System.getProperty("os.name");
        boolean isWindows = os.startsWith("Windows");
        boolean isJava14 =
            System.getProperty("java.version").equals("1.4") ||
            System.getProperty("java.version").startsWith("1.4.");
        String result = (isWindows)
                ? "cmd.exe /Q"
                : "bash";
        if(!isJava14){ //getenv does not work in java14
            if(isWindows){
                try{
                    String comspec = System.getenv("COMSPEC");
                    if(comspec != null){
                        if(comspec.endsWith("command.com")){ /* command.com */
                            result = comspec;
                        } else if(comspec.endsWith("cmd.exe")){ /* use cmd.exe */
                            result = comspec + " /Q"; /* disable command echo */
                        }
                    }
                } catch (Throwable ignore){
                    /* System.getEnv raises a java.lang.Error in Sun Java 1.4 */
                }
            } else {
                /* assume we're using a unix flavour */
                try {
                    /* look for SHELL environment variable */
                   String shell = System.getenv("SHELL");
                   if(shell != null){
                       result = shell;
                   }
                } catch (Throwable ignore){
                    /* System.getEnv raises a java.lang.Error in Sun Java 1.4 */
                }
            }
            if(result != null && result.endsWith("bash")){
                result += " -l";
            }
        }
        return result;
    }

    /** Returns the control actions for this ProcessIOPane.
    * The first element in the returned array calls {@link #interrupt}
    * in the body of its acionPerformed method. The second element
    * removes all output from the output area of this IOPane.
    **/
    public Action[] getControlActions(){
        return new Action[]{ interruptAction, clearAction};
    }

    public Action getSystemShellAction(){
        return systemShellAction;
    }

    /**
     * Creates a new ProcessIOPane.
     *
     * @param rows number of text rows >= 0
     * @param cols number of text columns >= 0 in a row
     * @throws IllegalArgumentException if the rows or columns arguments are negative.
     */
     public ProcessIOPane(int rows, int cols){
         super(rows, cols);
         init();
     }

     // static public void main(String[] argv){
         // SwingUtilities.invokeLater(new Runnable(){
             // public void run(){
                 // ProcessIOPane.test();
             // }
         // });
    // }
//
    // static private void test(){
        // final ProcessIOPane iop = new ProcessIOPane(50,50);
        // JFrame f = new JFrame(){
            // /**Overridden so we can exit when window is closed*/
            // protected void processWindowEvent(WindowEvent e) {
                // super.processWindowEvent(e);
                // if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    // iop.interrupt();
                    // System.exit(0);
                // }
            // }
        // };
        // f.getContentPane().add(iop);
        // f.pack();
        // f.setVisible(true);
        // ProcessRunnable test = new ProcessRunnable("cat C:\\test.txt");
        // iop.enqueue(test);
    // }

    /** Returns the RunnableQueue in which this ProcessIOPane stores jobs
    * awaiting execution. Jobs that you enqueue directly to the returned
    * RunnableQueue will not have their output and error streams redirected
    * to this IOPane by default.
    * @return the RunnableQueue assiociated with this ProcessIOPane
    * @see #enqueue
    **/
    public RunnableQueue getRunnableQueue(){
        return myProcesses;
    }

    /** Enqueues and executes the specified job as if it had been
    * started from the input field of this ProcessIOPane.
    * Jobs that you enqueue with this method will
    * have their output and error streams redirected to
    * and will get input from this IOPane.
    * If <code> command.getWorkingDirectory() == null </code>
    * the working directory of the command will be set to the working directory
    * of this ProcessIOPane.
    * @param command the job to enqueue and execute
    * @see #getRunnableQueue
    * @see RunnableQueue#enqueue
    **/
    public void enqueue(ProcessRunnable command){
        if(pwd != null && command.getWorkingDirectory() == null){
            command.setWorkingDirectory(pwd);
        }
        command.redirectOutputTo(iopOut);
        command.redirectErrorsTo(iopOut);
        myProcesses.enqueue(command);
        if(!myProcesses.isRunning()){
            myProcesses.start();
        }
    }

    /** Runs the specified job in the calling thread.
    * Jobs that you run with this method will
    * have their output and error streams redirected to this IOPane.
    * They will not be able to accept input from this IOPane.
    **/
     public void runInCallerThread(ProcessRunnable command){
         command.redirectOutputTo(iopOut);
         command.redirectErrorsTo(iopOut);
         command.setWorkingDirectory(pwd);
         try{
             command.run();
         } catch (Exception ex){
             printExceptionHandler.handleException(ex);
         }
     }

     /** Sets the working directory of new processes started from this
     * IOPane. The working directory can be set interactively by using
     * the 'cd' built-in.
     */
     public void setWorkingDirectory(String path){
         final String ppath =
            (path == null || path.length() == 0)
            ? "."
            : path;
         try{
            File newPwd = new File(ppath);
            if (pwd != null && !newPwd.isAbsolute()){
                newPwd = new File(pwd, ppath);
            }
            if(newPwd.isDirectory()){
                pwd = newPwd.getCanonicalFile();
            } else {
                append("Argument to cd must be a directory.\n");
                setWorkingDirectory(".");
            }
         } catch (Exception ex){
             printExceptionHandler.handleException(ex);
         }
     }

     public String getWorkingDirectory(){
         return (pwd == null)? null : pwd.toString();
     }

     /** returns a new OutputStream that writes to the output area */
     public OutputStream getOutputStream(){
         return new DocumentOutputStream(getDocument());
     }

     private void execute(String command){
         if(command == null){
             return;
         }
         String strim = command.trim();

         if (myProcesses.isRunning()){ /* forward input to currently running process */
             try{
                 String s = command.endsWith("\n")
                    ? command
                    : command+"\n";
                 myProcesses.handleInput(s);
             } catch (Exception ex){
                 ex.printStackTrace(new PrintStream(iopOut));
             }

         } else if (strim.length() == 0 || builtin(strim)){
             /* do nothing for empty command
              * execute builtins */
         } else if (strim.endsWith("&")){ /* new background process */
             strim = strim.substring(0,strim.length()-1).trim();
             runInBackground(new ProcessRunnable(strim));

         } else { /* new foreground process */
             enqueue(new ProcessRunnable(strim));
         }
     }

    /** Checks whether <code>s</code> is a built-in command and if so
     * executes it.
     **/
    protected boolean builtin(String s){
        boolean result = true;
        if ("clear".equals(s)){ /* clear textarea */
             clearAction.actionPerformed(null);

        } else if (s.matches("^cd(\\s.+)?")){ /* change working directory */
             String path = s.substring(2).trim();
             setWorkingDirectory(path);
             append(GLOBALS.getString("Working directory is ")+pwd.toString()+"\n");
        } else {
            result = false;
        }
        return result;
    }

    protected Preferences getPref(){
        return Preferences.userNodeForPackage(ProcessIOPane.class);
    }

     private void init(){
         systemShell = getPref().get("systemShell","");
         removeActionListener(echoAL);
         addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 execute(e.getActionCommand());
             }
         });
        addActionListener(echoAL);
        myProcesses.registerExceptionHandler(printExceptionHandler);
        myProcesses.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                prependPrompt = !myProcesses.isRunning();
            }
        });
        getInput().getInputMap().put(KeyStroke.getKeyStroke("ctrl C"),"ir");
        getInput().getActionMap().put("ir", interruptAction);
        getOutputArea().setFont(new Font("Monospaced",Font.PLAIN,12));
     }

     private void runInBackground(final ProcessRunnable command){
         (new Thread(){
             public void run(){
                 runInCallerThread(command);
             }
         }).start();
     }

     /** Interrupts the currently running process and deletes all jobs
     * awaiting execution. **/
     public void interrupt(){
         myProcesses.stop();
         append("[kill]\n");
     }
 }