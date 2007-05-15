package de.mospace.swing;

/*
 *  $Id: ExtensionInstaller.java,v 1.15 2007/02/18 14:20:22 ringler Exp $
 *  This class is part of the de.mospace.swing library.
 *  Copyright (C) 2006 Moritz Ringler
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
//To Do: check if we have a Sun JRE, if not warn that LaFInstaller might not work
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.Box;
import javax.swing.JTextArea;
import de.mospace.lang.ClassLoaderProvider;
/* TO DO:
 * Offer a choice of target directories:
 * - ${java.ext.dirs}
 * - ${user.home}/.${appname}/lib
 * - ${user.dir}/lib
 * - ${getRepositoryRoot(foo.bar.App.class)}/lib
 */
/**
 * Description of the Class
 *
 * @author ringler
 */
public class ExtensionInstaller {
    private static String javaext = null;
    private Component parent = null;
    private File targetDirectory;
    private File fallbackTargetDirectory;

    /** @deprecated use
    {@link de.mospace.lang.DefaultClassLoaderProvider#isTemporary}
    instead. **/
    public static boolean isTemporary(File file){
        boolean result = false;
        try{
            String tempdir = System.getProperty("java.io.tmpdir");
            tempdir = (new File(tempdir)).getCanonicalPath();
            result = file.getCanonicalPath().startsWith(tempdir);
        } catch (IOException ignore){}
        return result;
    }


    /**
     * Constructs a new ExtensionInstaller.
     *
     * @param parent a parent component for dialogs opened by this installer
     */
    public ExtensionInstaller(Component parent) {
        this.parent = parent;
    }


    /**
     * Returns the value of the target directory property. If the target
     * directory is not currently set it will be set by this method call as
     * follows. If a writable extension directory can be found it is used. If no
     * such directory exists the fallbackTargetDirectory is used if it has been
     * set and is writable. Otherwise, the user is prompted to specify a
     * writable extension directory.
     *
     * @return this installer's target directory if it is writable or null
     *      otherwise
     */
    public synchronized File getTargetDirectory() {
        if (targetDirectory == null) {
            String wed = getWritableExtensionDirectoryImpl();
            if (wed == null) {
                if (fallbackTargetDirectory != null) {
                    if (!fallbackTargetDirectory.exists()) {
                        fallbackTargetDirectory.mkdirs();
                    }
                    if (canWrite(fallbackTargetDirectory)) {
                        targetDirectory = fallbackTargetDirectory;
                    }
                } else {
                    wed = getWritableExtensionDirectoryFallback();
                }
            }
            if (wed != null) {
                targetDirectory = new File(wed);
            }
        }
        return (canWrite(targetDirectory)) ? targetDirectory : null;
    }


    /**
     * Sets this installer's target directory.
     *
     * @param dir the new value for the target directory property
     * @throws IllegalArgumentException if dir exists and is not a directory
     * @see #getTargetDirectory()
     */
    public void setTargetDirectory(File dir) {
        if (dir != null && dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getPath() + "is not a directory.");
        }
        targetDirectory = dir;
    }


    /**
     * Sets this installer's fallback target directory.
     *
     * @param dir the new value for the fallback target directory
     * @throws IllegalArgumentException if dir exists and is not a directory
     * @see #getTargetDirectory()
     */
    public void setFallbackTargetDirectory(File dir) {
        if (dir != null && dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getPath() + "is not a directory.");
        }
        fallbackTargetDirectory = dir;
    }


    /**
     * Returns this installer's fallback target directory.
     *
     * @return the fallback target directory or null if it has not been set
     * @see #getTargetDirectory()
     */
    public File getFallbackTargetDirectory() {
        return fallbackTargetDirectory;
    }


    /**
     * @return an existing, user-writable extension directory or <code>null</code>
     *      if the user cancelled the dialog
     */
    public String getWritableExtensionDirectory() {
        synchronized (ExtensionInstaller.class) {
            if (javaext == null) {
                javaext = getWritableExtensionDirectoryImpl();
            }
            if (javaext == null) {
                javaext = getWritableExtensionDirectoryFallback();
            }
        }
        return javaext;
    }


    private String getWritableExtensionDirectoryFallback() {
        File result = null;
        String libext = System.getProperty("java.ext.dirs");
        do {
            result = queryFile(GLOBALS.getMessage(
                    "EXT_INST_QUERY_FILE_PROMPT",
                    libext + File.pathSeparatorChar),
                    GLOBALS.getString("EXT_INST_QUERY_FILE_TITLE"),
                    JFileChooser.DIRECTORIES_ONLY);
        } while (result != null &&
                !(result.isDirectory() && canWrite(result)));
        return (result == null) ? null : result.getAbsolutePath();
    }

    private String getWritableExtensionDirectoryImpl() {
        String result = null;
        String libext = System.getProperty("java.ext.dirs");
        String[] candidates = libext.split(File.pathSeparator);
        File f;
        for (int i = 0; i < candidates.length; i++) {
            f = new File(candidates[i]);
            if (canWrite(f)) {
                result = f.getAbsolutePath();
                break;
            }
        }
        return result;
    }


    /**
     * Tests whether we can write to the given file or directory.
     *
     * @param test the file to test
     * @return wether we can modify or create the given file
     */
    public static boolean canWrite(File test) {
        //File.canWrite is broken on NTFS
        if(test == null) {
            return false;
        }

        // if f does not exist we test if we can write to its parent directory
        if(!test.exists()){
            try {
                return canWrite(test.getCanonicalFile().getParentFile());
            } catch (IOException notcanonical) {
                return false;
            }
        }


        boolean canWrite = true;
        if (test.isFile()) {

            // if test exists and is a file we try if we can open it for writing
            try {
                FileOutputStream fos = new FileOutputStream(test, true);
                fos.close();
            } catch (FileNotFoundException ex) {
                //System.out.println("Cannot write 1: "+ex.getMessage());
                //we cannot write
                canWrite = false;
            } catch (IOException ex) {
                //we did not succeed in closing fos...
                ex.printStackTrace();
            }

        } else if (test.isDirectory()) {
            // if test exists and is a directory we try if we can create a temp file
            File tmp = null;
            try {
                tmp = File.createTempFile("aaa", null, test);
            } catch (IOException ex) {
                //we did not succeed in creating the tmp file
                canWrite = false;
                //System.out.println("Cannot write 2: "+ex.getMessage());
            }
            //clean up
            if (tmp != null && tmp.exists()) {
                tmp.delete();
            }
        } else if (!test.exists()) {
            //f and its parent file do not exist
            canWrite = false;
            //System.out.println("Cannot write 3: Neither " + f.toString() +" nor its parent directory exist.");
        }
        return canWrite;
    }


    /**
     * Asks the user to provide a File path.
     *
     * @param message the message text of the input dialog.
     * @param title the title of the input dialog
     * @param selectionmode one of JFileChooser.FILES_ONLY,
     *      FileChooser.DIRECTORIES_ONLY and JFileChooser.FILES_AND_DIRECTORIES
     * @return A new File object or <code>null</code> if the user cancelled the
     *      dialog.
     */
    protected File queryFile(String message, String title, int selectionmode) {
        Box dialogcp = Box.createVerticalBox();
        JTextArea messageArea = new JTextArea(message, 5, 80);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        dialogcp.add(messageArea);
        PathInput pinz = new PathInput("", selectionmode);
        dialogcp.add(pinz);
        JOptionPane pane = new JOptionPane(dialogcp,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION);
        JDialog d = pane.createDialog(parent, title);
        d.pack();
                d.setResizable(true);
        d.setVisible(true);
        d.pack();
        return (
            (new Integer(JOptionPane.OK_OPTION)).equals(pane.getValue())
                )
                 ? new File(pinz.getPath())
                 : null;
    }


    /**
     * Displays a warning dialog with the given title and message.
     *
     * @param message the warning message
     * @param title the dialog title
     */
    protected void warn(String message, String title) {
        JOptionPane.showMessageDialog(parent,
                message, title,
                JOptionPane.WARNING_MESSAGE);
    }


    /**
     * Asks the user to answer yes or no to the given question.
     *
     * @param question the dialog message
     * @param title the dialog title
     * @return true for yes and false for no or if the user cancelled the dialog
     */
    protected boolean ask(String question, String title) {
        return (JOptionPane.showConfirmDialog(parent, question, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                 == JOptionPane.YES_OPTION);
    }


    /**
     * Tests two jar files for identical contents.
     *
     * @param in one of the files to compare
     * @param myOut the other jar file
     * @return true if both files have identical contents false otherwise
     * @throws IOException if an I/O error has occurred
     */
    private static boolean jarFileEquals(File in, File myOut) throws IOException {
        /*
         *  test if both files have equal size
         */
        if (in.length() != myOut.length()) {
            return false;
        }

        JarFile jin = new JarFile(in);
        JarFile jout = new JarFile(myOut);

        /*
         *  test if both jars have the same manifests
         */
        if (!jin.getManifest().equals(jout.getManifest())) {
            return false;
        }

        List inEntries = Collections.list(jin.entries());
        List outEntries = Collections.list(jout.entries());

        /*
         *  test for same number of entries
         */
        if (inEntries.size() != outEntries.size()) {
            return false;
        }

        /*
         *  test entry names, checksums and modification times for equality
         */
        Comparator comp =
            new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((ZipEntry) o1).getName().compareTo(
                            ((ZipEntry) o2).getName());
                }
            };
        Collections.sort(inEntries, comp);
        Collections.sort(outEntries, comp);

        Iterator iin = inEntries.iterator();
        Iterator iout = outEntries.iterator();
        ZipEntry zin;
        ZipEntry zout;

        while (iin.hasNext()) {
            zin = (ZipEntry) iin.next();
            zout = (ZipEntry) iout.next();
            if (!zin.getName().equals(zout.getName())) {
                return false;
            }
            if (zin.getCrc() != zout.getCrc()) {
                return false;
            }
            if (zin.getTime() != zout.getTime()) {
                return false;
            }
        }

        return true;
    }


    /**
     * Copies in to out using nio channels.
     * This method will not close the streams.
     *
     * @param in the source file input stream
     * @param out the destination file output stream
     * @throws IOException if an I/O error occurs
     */
    private void transfer(FileInputStream in, FileOutputStream out) throws IOException {
        /*
         *  copy using nio channels
         */
        FileChannel sourceChannel = in.getChannel();
        FileChannel destinationChannel = out.getChannel();
        FileLock targetLock = destinationChannel.tryLock();
        if (targetLock == null) {
            throw new IOException("Cannot lock destination file.");
        }
        try {
            sourceChannel.transferTo(0, sourceChannel.size(),
                    destinationChannel);
        } finally {
            targetLock.release();
        }
    }


    /**
     * Tests whether f is writable and asks the user to provide a different
     * file if not.
     *
     * @param f the file to test
     * @return f or the file specified by the user
     */
    private File checkWritable(File f) {
        File myOut = f;
        if (!canWrite(myOut)) {
            String sOut = JOptionPane.showInputDialog(parent,
                    GLOBALS.getMessage("EXT_INST_WRITE_ERROR_PROMPT",
                    new Object[]{myOut.getName(), myOut.getParent()}),
                    GLOBALS.getString("EXT_INST_WRITE_ERROR_TITLE"));
            if (sOut == null) {
                return null;
            }
            if (!sOut.endsWith(".jar")) {
                sOut += ".jar";
            }
            File extdir = getTargetDirectory();
            if (extdir == null) {
                return null;
            }
            myOut = new File(extdir, sOut);
        }
        return myOut;
    }


    /**
     * Installs a Java extension.
     *
     * @param extjar the jar file containing the extension classes you want to
     *      install
     * @return true if the jar file was successfully installed
     */
    public boolean installExtension(File extjar) {
        return installExtension(extjar, (ClassLoaderProvider) null);
    }

    public boolean installExtension(File extjar, ClassLoaderProvider clp){
        try {

            /*
             *  Construct writable output file
             */
            File extdir = getTargetDirectory();
            if (extdir == null) {
                warn("No valid target directory.", "Installation failed");
                return false;
            }
            if(!extdir.exists()){
                extdir.mkdirs();
            }
            File[] jars = extdir.listFiles();
            for(int i=0; i<jars.length; i++){
                if(jars[i].getName().endsWith(".jar") &&
                        jarFileEquals(extjar, jars[i])){
                    return true;
                }
            }

            File myOut = new File(extdir, extjar.getName());
            boolean repeat = false;
            do {
                repeat = false;
                if (myOut.exists()) {
                    if (jarFileEquals(extjar, myOut)) {
                        return true;
                    }
                    if (!ask(GLOBALS.getMessage("EXT_INST_OVERWRITE_PROMPT_A",
                            extjar.getName()),
                            GLOBALS.getString("EXT_INST_OVERWRITE_TITLE"))) {
                        return false;
                    }
                    File f = checkWritable(myOut);
                    if (f == null) {
                        warn("Cannot write to file " + myOut, "Installation failed");
                        return false;
                    } else if (!f.equals(myOut)) {
                        repeat = true;
                        myOut = f;
                    }
                }
            } while (repeat);

            /*
             *  copy using nio channels
             */
            FileInputStream fis = new FileInputStream(extjar);
            try {
                FileOutputStream fos = new FileOutputStream(myOut);
                try {
                    transfer(fis, fos);
                } finally {
                    fos.close();
                }
            } finally {
                fis.close();
            }
            if (myOut.exists()) {
                /*
                 *  preserve modification time
                 */
                myOut.setLastModified(extjar.lastModified());
                if(clp != null && myOut.getName().endsWith(".jar")){
                    clp.registerLibrary(myOut);
                }
            }
        } catch (Exception ex) {
            warn(ex.toString(), "Installation failed");
            return false;
        }
        return true;
    }


    /**
     * Installs a Java extension contained in a zip file.
     *
     * @param zip the zip archive that contains the extension jar file
     * @param extjars the path to the extension jar file relative to the zip root
     * @return whether the installation succeeded
     */
    public boolean installExtension(File zip, String extjars) {
        return installExtension(zip, extjars, null);
    }

    public boolean installExtension(File zip, String extjars, ClassLoaderProvider clp) {
        try {
            ZipFile extZ = new ZipFile(zip);
            ZipEntry extjar = extZ.getEntry(extjars);
            if (extjar == null) {
                return false;
            }

            /*
             *  Construct writable output file
             */
            File extdir = getTargetDirectory();
            if (extdir == null) {
                return false;
            }
            if(!extdir.exists()){
                extdir.mkdirs();
            }
            File myOut = new File(extdir, (new File(extjars)).getName());
            boolean repeat = false;
            do {
                repeat = false;
                if (myOut.exists()) {
                    if (!ask(GLOBALS.getMessage("EXT_INST_OVERWRITE_PROMPT_B",
                            extjars),
                            GLOBALS.getString("EXT_INST_OVERWRITE_TITLE"))) {
                        return false;
                    }
                    File f = checkWritable(myOut);
                    if (f == null) {
                        return false;
                    } else if (!f.equals(myOut)) {
                        repeat = true;
                        myOut = f;
                    }
                }
            } while (repeat);

            int count;
            int BUFFER = 2048;
            byte data[] = new byte[BUFFER];

            InputStream is = extZ.getInputStream(extjar);
            is = new BufferedInputStream(is);

            try {
                FileOutputStream fos = new FileOutputStream(myOut);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                try {
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                } finally {
                    dest.close();
                }

            } finally {
                is.close();
            }

            if (myOut.exists()) {
                /*
                 *  preserve modification time
                 */
                myOut.setLastModified(extjar.getTime());
                if(clp != null && myOut.getName().endsWith(".jar")){
                    clp.registerLibrary(myOut);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
