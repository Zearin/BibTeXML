package de.mospace.swing;
/*
 *  $Id: LaFInstaller.java,v 1.11 2007/01/17 21:05:59 ringler Exp $
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
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import de.mospace.lang.RTSI;
import de.mospace.lang.ClassLoaderProvider;

/**
 * This class provides a convenient way to install new LookAndFeels in a Sun
 * JRE. Given an archive with a LookAndFeel it will try to copy the jar file to
 * a java extensions directory as per the system property <code>java.ext.dirs</code>
 * or a client specified directory and add an entry to the property file
 * <code>swing.properties</code>.<br>
 * If the installation was succesfull, the LookAndFeel will be accessible
 * through {@link UIManager#getInstalledLookAndFeels} with the next invocation
 * of the JVM. In particular, applications can let the user switch to the new
 * LookAndFeel with a {@link LookAndFeelMenu}.
 *
 * @author Moritz Ringler
 */
public final class LaFInstaller extends ExtensionInstaller {
    private static String javahome = System.getProperty("java.home");
    private final Properties swingp =
        new Properties() {
            /*
             *  This hack causes sorted output, if store()
             *  uses keys() to iterate over properties.
             *  Since this is not necessarily so, this is
             *  just a hack and it would be cleaner (but
             *  much lengthier) to overwrite store().
             *  It will do no harm, if keys() is not used.
             */
            public Enumeration keys() {
                Enumeration ken = super.keys();
                List kli = Collections.list(ken);
                Collections.sort(kli);
                return Collections.enumeration(kli);
            }

            /*
             *  This hack causes sorted output, if store()
             *  uses keySet() to iterate over properties
             *  (as GNU Classpath 0.91 does).
             *  Since this is not necessarily so, this is
             *  just a hack and it would be cleaner (but
             *  much lengthier) to overwrite store().
             *  /Breaks the general contract of keySet which requires
             *  /that changes in the Hashtable must reflect in the Set
             *  /returned by keySet
             *  public Set keySet(){
             *  return new TreeSet(super.keySet());
             *  }
             */
        };
    private static File swingpf = new File(new File(javahome, "lib"), "swing.properties");
    private final Set installedlafs = new HashSet();
    private File fallbackProps = null;


    /**
     * Constructs a new LaFInstaller with a <code>null</code> parent component.
     */
    public LaFInstaller() {
        this(null);
    }


    /**
     * Constructs a new LaFInstaller.
     *
     * @param parent The parent component to use for dialogs. Can be <code>null</code>
     */
    public LaFInstaller(Component parent) {
        super(parent);
        init();
        //System.out.println("installedlafs:" + installedlafs.toString());
    }

    public LaFInstaller(Component parent, File installdir, File lafProperties){
        super(parent);
        fallbackProps = lafProperties;
        setFallbackTargetDirectory(installdir);
        init();
    }

    private void init(){
        readProps(swingpf);
        readProps(fallbackProps);
        if(installedlafs.isEmpty()){
            LookAndFeelInfo[] lafi = UIManager.getInstalledLookAndFeels();
            for (int i = 0; i < lafi.length; i++) {
                installedlafs.add(new LaFPackage(lafi[i]));
            }
        }
        updateProperties();
    }

    private void readProps(File propf){
        if (propf != null && propf.isFile()) {
            InputStream fips = null;
            try {
                fips = new BufferedInputStream(new FileInputStream(propf));
                swingp.load(fips);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } finally {
                if (fips != null) {
                    try {
                        fips.close();
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    }
                }
            }
        }
        String silafs = swingp.getProperty("swing.installedlafs");
        if (silafs != null) {
            String[] lafs = silafs.split("\\s*,\\s*");
            for (int i = 0; i < lafs.length; i++) {
                installedlafs.add(new LaFPackage(null,
                        swingp.getProperty("swing.installedlaf." + lafs[i] +
                        ".class"),
                        swingp.getProperty("swing.installedlaf." + lafs[i] +
                        ".name",
                        lafs[i])));
            }
        }
    }


    /**
     * Allows direct invocation of the LaFInstaller from the command line.
     *
     * @param argv No arguments are expected.
     * @throws IOException if an I/O error occurred during modification
     * of swing.properties.
     */
    public static void main(String[] argv) throws IOException {
        Handler lh = new ConsoleHandler();
        lh.setLevel(Level.SEVERE);
        RTSI.addLogHandler(lh);
        LaFInstaller me = new LaFInstaller();
        me.installLaF(me.queryLaF());
        /*
         *  The explicit exit is necessary in J2RE 1.4.1_02
         */
        System.exit(0);
    }


    /**
     * Returns whether we can install new LookAndFeels.
     *
     * @return a boolean indicating whether we can modify the swing.properties
     *      file.
     */
    public static boolean canInstall(File props) {
        return canWrite(swingpf) ||
            (props != null) && canWrite(props) ;
    }

    /**
     * Returns whether we can install new LookAndFeels.
     *
     * @return a boolean indicating whether we can modify the swing.properties
     *      file.
     */
    public static boolean canInstall() {
        return canInstall(null);
    }


    /**
     * Checks whether we can install new LookAndFeels and displays a dialog with
     * an error message if not.
     *
     * @param pparent Description of the Parameter
     */
    public static void warnCannotInstall(Component pparent, File iprops) {
        if (!canInstall(iprops)) {
            JOptionPane.showMessageDialog(pparent,
                    GLOBALS.getMessage("LAF_INST_CANNOT_INSTALL_WARNING",
                    swingpf.getAbsolutePath()),
                    GLOBALS.getString("LAF_INST_CANNOT_INSTALL_TITLE"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }


    /**
     * Aks the user to specify an archive wich will be searched for
     * LookAndFeels as per {@link LaFPackage#fromJar} and
     * {@link LaFPackage#fromZip}. If no LaF is found displays a warning
     * message and returns. If more than one LookandFeel is found
     * asks the user to pick one.
     *
     * @return the LookAndFeel specified by the user or null if no LookAndFeel
     * has been found, the user cancelled a dialog or an I/O error occurred.
     */
    public LaFPackage queryLaF(){
        LaFPackage result = null;
        try{
        File in = queryFile(GLOBALS.getMessage("LAF_INST_PACKAGE_PROMPT", System.getProperty("java.version")),
                GLOBALS.getString("LAF_INST_PACKAGE_TITLE"),
                JFileChooser.FILES_ONLY);
        if (in == null) {
            return null;
        }
        String ppath = in.getPath();
        if (!in.isFile()) {
            warn(GLOBALS.getMessage("LAF_INST_INVALID_PACKAGE", ppath),
                    GLOBALS.getString("LAF_INST_INVALID_FILE_TITLE"));
            return null;
        }
        LaFPackage[] lafp = null;
        if (ppath.endsWith(".zip")) {
            lafp = LaFPackage.fromZip(in);
            //System.err.println(Arrays.asList(lafp));
        } else if (ppath.endsWith(".jar")) {
            lafp = LaFPackage.fromJar(in);
        } else {
            warn(GLOBALS.getMessage("LAF_INST_NO_ZIP", ppath),
                    GLOBALS.getString("LAF_INST_INVALID_FILE_TITLE"));
        }
        if (lafp != null) {
            if (lafp.length == 0) {
                warn(GLOBALS.getMessage("LAF_INST_NO_LAF", ppath),
                        GLOBALS.getString("LAF_INST_NO_LAF_TITLE"));
            } else {
                result = (lafp.length > 1) ? pickLaF(lafp) : lafp[0];
            }
        }
        } catch (Exception ioerror){
            System.err.println(ioerror.getMessage());
            System.err.flush();
            result = null;
        }
        return result;
    }


    /**
     * Asks the user to pick one of several LookAndFeels.
     *
     * @param lafs the LookAndFeels to choose from.
     * @return the selected LookAndFeel or <code>null</code> if the user
     *      cancelled the dialog.
     */
    public LaFPackage pickLaF(LaFPackage[] lafs) {
        String[] names = new String[lafs.length];
        for (int i = 0; i < lafs.length; i++) {
            names[i] = lafs[i].getName() + " (" +
                    lafs[i].getClassName() + " in " + lafs[i].getJar().getName() + ")";
        }
        String lafname = (String) JOptionPane.showInputDialog(parent,
                GLOBALS.getString("LAF_INST_PICK_LAF_PROMPT"),
                GLOBALS.getString("LAF_INST_PICK_LAF_TITLE"),
                JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
        return (lafname == null) ? null : lafs[Arrays.asList(names).indexOf(lafname)];
    }


    /**
     * Tries to install the specified LookAndFeel and returns whether the
     * installation was successful. More specifically, installs the jar file
     * returned by {@link LaFPackage#getJar laf.getJar()} to this installer's
     * target directory and modifies <code>swing.properties</code> to include
     * the LookAndFeel referenced by <code>jar</code>.
     *
     * @param laf The LookAndFeel to install. May be <code>null</code>.
     * @return whether the jar file was copied and swing.properties was modified
     *      to include the new LookAndFeel.
     * @exception IOException when an error occurs during modification of <code>swing.properties</code>
     *      .
     */
    public boolean installLaF(LaFPackage laf) throws IOException {
        return installLaF(laf, null);
    }

    public boolean installLaF(LaFPackage laf, ClassLoaderProvider clp) throws IOException {
        warnCannotInstall(parent, fallbackProps);
        boolean result = false;
        if (laf != null) {
            if (installedlafs.contains(laf)) {
                warn(GLOBALS.getMessage("LAF_INST_LAF_ALREADY_INSTALLED",
                        new Object[]{laf.toString(), laf.getName()}),
                        GLOBALS.getString(
                        "LAF_INST_LAF_ALREADY_INSTALLED_TITLE"));
            } else if (installLaFJar(laf.getJar(), clp)) {
                installedlafs.add(laf);
                dump();
                result = true;
            }
        }
        return result;
    }


    /**
     * Calling this method on a LafInstaller will cause an
     * UnsupportedOperationException. Use {@link #installLaF} if you want to
     * install a LookAndFeel or a generic {@link ExtensionInstaller} if you want
     * to install a different kind of extension.
     *
     * @param jarFile ...
     * @return ...
     * @throws UnsupportedOperationException
     */
    public boolean installExtension(File jarFile) {
        throw new UnsupportedOperationException();
    }


    /**
     * Calling this method on a LafInstaller will cause an
     * UnsupportedOperationException. Use {@link #installLaF} if you want to
     * install a LookAndFeel or a generic {@link ExtensionInstaller} if you want
     * to install a different kind of extension.
     *
     * @param zip ...
     * @param extjars ...
     * @return ...
     * @throws UnsupportedOperationException
     */
    public boolean installExtension(File zip, String extjars) {
        throw new UnsupportedOperationException();
    }


    /**
     * Description of the Method
     *
     * @param lafJar Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException Description of the Exception
     */
    private boolean installLaFJar(File lafJar, ClassLoaderProvider clp)
            throws IOException {
        return (lafJar == null) ? false : super.installExtension(lafJar, clp);
    }


    /** Updates swingp with the current state of installedlafs. */
    private void updateProperties() {
        LaFPackage lp;
        String laf;
        StringBuffer ilprop = new StringBuffer(installedlafs.size() * 20);
        Iterator it = installedlafs.iterator();
        while (it.hasNext()) {
            lp = (LaFPackage) it.next();
            laf = lp.toString();
            ilprop.append(laf).append(',');
            swingp.setProperty("swing.installedlaf." + laf + ".class",
                    lp.getClassName());
            swingp.setProperty("swing.installedlaf." + laf + ".name",
                    lp.getName());
        }
        ilprop.deleteCharAt(ilprop.length() - 1);
        swingp.setProperty("swing.installedlafs", ilprop.toString());
    }


    /** Updates UIManagers installedLookAndFeels with the current state of installedlafs. */
    public void updateUIManagerInstalledLafs() {
        UIManager.setInstalledLookAndFeels(
            (LookAndFeelInfo[]) installedlafs.toArray(
                new LookAndFeelInfo[installedlafs.size()]
            )
        );
    }


    /**
     * Updates swing.properties from the current state of installedlafs.
     *
     * @exception IOException Description of the Exception
     */
    private void dump() throws IOException {
        updateProperties();
        OutputStream fops = null;
        File target = null;
        if(canWrite(swingpf)){
            target = swingpf;
        } else if (fallbackProps != null && canWrite(fallbackProps)){
            target = fallbackProps;
        }
        try {
            fops = new BufferedOutputStream(new FileOutputStream(target));
            swingp.store(fops, "swing.properties");
        } finally {
            if (fops != null) {
                fops.close();
            }
        }
    }


    /**
     * Description of the Class
     *
     * @author ringler
     */
    public final static class LaFPackage extends UIManager.LookAndFeelInfo {
        private final File lafjar;


        /**
         * Constructs a new LaFPackage.
         *
         * @param jar Description of the Parameter
         * @param mainclass Description of the Parameter
         * @param name Description of the Parameter
         */
        public LaFPackage(File jar, String mainclass, String name) {
            super(name, mainclass.replaceFirst("\\.class$", ""));
            lafjar = jar;
        }


        /**
         * Constructs a new LaFPackage.
         *
         * @param info Description of the Parameter
         */
        public LaFPackage(LookAndFeelInfo info) {
            super(info.getName(), info.getClassName());
            lafjar = null;
        }


        /**
         * Description of the Method
         *
         * @param obj Description of the Parameter
         * @return Description of the Return Value
         */
        public boolean equals(Object obj) {
            return (obj instanceof LaFPackage) && (obj.toString().equals(this.toString()));
        }


        /**
         * Description of the Method
         *
         * @return Description of the Return Value
         */
        public int hashCode() {
            return this.toString().hashCode();
        }


        /**
         * Gets the jar attribute of the LaFPackage object. It is not guaranteed
         * that the File object returned actually exists.
         *
         * @return The jar value
         */
        public File getJar() {
            return lafjar;
        }


        /**
         * Description of the Method
         *
         * @return Description of the Return Value
         */
        public String toString() {
            String result = getClassName();
            result.replaceFirst("\\.class$", "");
            //unnecessary but does no harm!
            int a = result.lastIndexOf('.');
            if (a >= 0) {
                result = result.substring(a + 1).replaceFirst("LookAndFeel", "");
            }
            return result.toLowerCase();
        }


        /**
         * Searches the given jar file for all LaFs it contains. As a side
         * effect the jar file may become write protected until the jvm exits.
         * Use {@link File#deleteOnExit deleteonExit} if a simple delete fails.
         * <pre>
         * if(! jar.delete()){
         *    jar.deleteOnExit();
         * }
         * </pre> Note that deleteOnExit might block memory that is released
         * only when the JVM exits.
         *
         * @param jar the jar file to search
         * @return all LookAndFeels found in the jar file
         */
        public static LaFPackage[] fromJar(File jar) {
            List result = new Vector();
            ZipFile zip = null;
            URLClassLoader myCl;
            try {
                /*
                 *  make a class loader that reads from the jar file
                 */
                myCl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
                //System.out.println(Arrays.asList(myCl.getURLs()));

                /*
                 *  initialize a new RTSI with this class loader
                 */
                RTSI myRTSI = new RTSI(myCl);

                /*
                 *  search the jarfile
                 */
                zip = new ZipFile(jar);
                Enumeration entries = zip.entries();

                while (entries.hasMoreElements()) {

                    /*
                     *  look for packages/directories
                     */
                    ZipEntry dir = (ZipEntry) entries.nextElement();
                    String dirName = dir.getName();
                    if (dir.isDirectory()) {
                        String packageName = dirName.substring(0, dirName.length() - 1).replaceAll("/", ".");

                        /*
                         *  look for LookAndFeels in a package
                         */
                        String[] lafclasses = myRTSI.find(packageName, LookAndFeel.class);
                        LaFPackage[] xresult = new LaFPackage[lafclasses.length];
                        for (int i = 0; i < xresult.length; i++) {
                            LookAndFeel laf = (LookAndFeel)
                                    Class.forName(lafclasses[i], true, myCl).newInstance();
                            xresult[i] = new LaFPackage(jar, lafclasses[i], laf.getName());
                        }

                        /*
                         *  store the LookAndFeels we have found in the result list
                         */
                        result.addAll(Arrays.asList(xresult));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            } finally {
                myCl = null;
                if (zip != null) {
                    try {
                        zip.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return (LaFPackage[]) result.toArray(new LaFPackage[result.size()]);
        }


        //method LaFPackage.fromJar

        /**
         * The returned LaFPackage's freeResources() method should be called
         * when none of them is needed any more.
         *
         * @param zipfile Description of the Parameter
         * @return Description of the Return Value
         * @exception IOException Description of the Exception
         */
        public static LaFPackage[] fromZip(File zipfile) throws IOException {
            List result = new Vector();
            BufferedInputStream is = null;
            BufferedOutputStream dest = null;

            /*
             *  IO config
             */
            int BUFFER = 2048;
            byte data[] = new byte[BUFFER];
            int count;

            /*
             *  create temporary directory
             */
            String zipname = zipfile.getName();
            if (zipname.length() < 7) {
                zipname = "xxxxxxx" + zipname;
            }

            File target = null;

            /*
             *  read zip file
             */
            ZipFile zip = new ZipFile(zipfile);
            Enumeration entries = zip.entries();
            ZipEntry jar;
            String jarName;

            while (entries.hasMoreElements()) {
                jar = (ZipEntry) entries.nextElement();
                jarName = jar.getName();

                if (jarName.endsWith(".jar")) {

                    /*
                     *  extract jar file to temporary directory
                     */
                    /*
                     *  get rid of jar directory structure
                     */
                    target = new File(jarName);
                    String jFileName = target.getName();

                    /*
                     *  construct tmp jarfile
                     */
                    /*
                     *  get the temporary directory
                     */
                    target = File.createTempFile("xxx", null);
                    target.delete();
                    target = target.getParentFile();

                    /*
                     *  construct the tmp jarfile name
                     */
                    target = new File(target, jFileName);
                    for (int i = 1; target.exists(); i++) {
                        target = new File(target.getParent(), jFileName.replaceFirst(".jar$", "-" + i + ".jar"));
                    }

                    target.deleteOnExit();

                    try {
                        is = new BufferedInputStream(zip.getInputStream(jar));
                        FileOutputStream fos = new FileOutputStream(target);
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                    } finally {
                        if (dest != null) {
                            dest.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    }

                    /*
                     *  look for LookAndFeels in jar
                     */
                    LaFPackage[] foundInJar = LaFPackage.fromJar(target);
                    if (foundInJar == null || foundInJar.length == 0) {
                        /*
                         *  if none are found delete the extracted jar
                         *  from the temporary directory ...
                         */
                        if (target != null) {
                            target.delete();
                            target = null;
                        }
                    } else {
                        /*
                         *  otherwise add the found LaFs to the result list
                         */
                        result.addAll(Arrays.asList(foundInJar));
                    }
                }
            }
            return (LaFPackage[]) result.toArray(new LaFPackage[result.size()]);
        }


        // method LaFPackage.fromZip

        /**
         * Description of the Method
         *
         * @param f Description of the Parameter
         * @return Description of the Return Value
         */
        private static boolean deleteRecursively(File f) {
            if (f.isDirectory()) {
                File[] ff = f.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    deleteRecursively(ff[i]);
                }
            }
            return f.delete();
        }
    }
    // Class LaFPackage


}
