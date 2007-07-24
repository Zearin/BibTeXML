/*
 *  $Id: LookAndFeelMenu.java,v 1.20 2007/01/17 21:05:59 ringler Exp $
 *  This class is part of the de.mospace.swing library.
 *  Copyright (C) 2005-2006 Moritz Ringler
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
package de.mospace.swing;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import de.mospace.lang.ClassLoaderProvider;
import de.mospace.lang.DefaultClassLoaderProvider;

/**
 * A JMenu that lets you change the LookAndFeel of a window. The default
 * menutitle is LnF. Use <code>setText()</code> to change it.<p>
 * An instance of this class will detect when the UIManager's lookAndFeel
 * property changes and will select the corresponding menu item if it exists.
 * No further action will be taken, in particular the LookAndFeelMenu will
 * not update the application GUI. In general, once a client creates
 * an instance of this class it should yield control over the LookAndFeel
 * to the LookAndFeelMenu.<p>
 * There should be no problem however to have several instances of this class.
 *
 * @see #LookAndFeelMenu(Preferences, Window, File, File, ClassLoader)
 *
 * @author Moritz Ringler
 * @version $Revision: 1.20 $ ($Date: 2007/01/17 21:05:59 $)
 */
public class LookAndFeelMenu extends JMenu {
    private List lafItems;
    private Preferences node = null;
    private Window win;
    private Component dialogParent = null;
    private ClassLoader cl = ClassLoader.getSystemClassLoader();
    private ClassLoaderProvider clp;
    private final ActionListener lnfListener =
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switchLaF((LaFMenuItem) e.getSource());
                    updateSelection(currentLaF());
                }
            };

    private final PropertyChangeListener LaFListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent e){
            if(
                e.getPropertyName().equals("lookAndFeel") &&
               !e.getNewValue().equals(e.getOldValue())
            ){
                updateSelection(e.getNewValue().getClass().getName());
            }
        }
    };
    ButtonGroup bg = new ButtonGroup();


    /**
     * Creates a new LookAndFeelMenu that does not remember the selected
     * LookAndFeel between program starts.<p>
     * Equivalent to
     * <code>{@link #LookAndFeelMenu(Preferences, Window, File, File, ClassLoader)
     * LookAndFeelMenu(null, w, null, null, null)}</code>.
     *
     * @param w the window so set the look and feel of. May be <code>null</code>.
     */
    public LookAndFeelMenu(Window w) {
        win = w;
        init(UIManager.getInstalledLookAndFeels(), null, null);
    }



    /**
     * Creates a new LookAndFeelMenu that remembers the selected LookAndFeel
     * between program starts.<p>
     * Equivalent to
     * <code>{@link
     * #LookAndFeelMenu(Preferences, Window, File, File, ClassLoader)
     * LookAndFeelMenu(pref, w, null, null, null)}</code>.
     *
     *
     * @param pref the preferences node to read from and write to
     * @param w the window so set the look and feel of, may be null
     */
    public LookAndFeelMenu(Preferences pref, Window w) {
        win = w;
        node = pref;
        init(UIManager.getInstalledLookAndFeels(), null, null);
    }


    /**
     * Creates a new LookAndFeelMenu that remembers the selected LookAndFeel
     * between program starts and uses a fallback mechanism if the user does not
     * have write access to the JRE directories. Look and Feel libraries
     * will be read from and installed as jar files in the parent directory
     * of lafProperties.<p>
     *
     * @param pref the preferences node to read from and write to
     * @param w the window so set the look and feel of, may be null
     * @param lafProperties a fallback properties file.
     * @see #LookAndFeelMenu(Preferences, Window, File, File, ClassLoader)
     */
    public LookAndFeelMenu(Preferences pref, Window w, File lafProperties) {
       this(pref, w, lafProperties,
            lafProperties.getAbsoluteFile().getParentFile(),
            initCLP(lafProperties.getAbsoluteFile().getParentFile()));
    }

    private static ClassLoaderProvider initCLP(File dir){
        ClassLoaderProvider newclp = new DefaultClassLoaderProvider();
        newclp.registerLibraryDirectories(new File[]{dir});
        return newclp;
    }

    /**
     * Creates a new LookAndFeelMenu that remembers the selected LookAndFeel
     * between program starts and uses a fallback mechanism if the user does not
     * have write access to the JRE directories.<p>
     * Note that if installdir is <code>null</code> the system class loader will
     * be used. Consider using <code>{@link
     *    #LookAndFeelMenu(Preferences pref, Window w, File lafProperties)}
     * </code>
     * in this case.<p>
     * Equivalent to<pre>
     * LookAndFeelMenu(pref,
     *                 w,
     *                 lafProperties,
     *                 installdir,
     *                 createLibClassLoader(installdir))</pre>
     *
     * @param pref the preferences node to read from and write to
     * @param w the window so set the look and feel of, may be null
     * @param lafProperties a fallback properties file. Can be <code>null</code>.
     * @param installdir a fallback installation directory.
     * @see #LookAndFeelMenu(Preferences, Window, File, File, ClassLoader)
     */
    public LookAndFeelMenu(Preferences pref, Window w, File lafProperties, File installdir) {
      this(pref, w, lafProperties, installdir, initCLP(installdir));
    }


    private void updateSelection(String newLafClass){
        Iterator it = lafItems.iterator();
        LaFMenuItem item;
        while(it.hasNext()){
            item = (LaFMenuItem) it.next();
            String itemclass = item.getLaFClassName();
            if(itemclass.equals(newLafClass)){
                item.setSelected(true);
                break;
            }
        }
    }

    /**
     * Creates a new LookAndFeelMenu that remembers the selected LookAndFeel
     * between program starts and uses a fallback mechanism if the user does not
     * have write access to the JRE directories.<p>
     *
     * If possible LookAndFeel jar files will be installed to the first writable
     * directory specified by the Java system property <tt>java.ext.dirs</tt> ,
     * and information about the installed LookAndFeels will be saved in <tt>
     * lib/swing.properties</tt> .<p>
     *
     * If the user does not have write access to the extension directories the
     * jar files will be installed to <tt>installdir</tt> or the user will be
     * asked for a target directory if both <tt>installdir</tt> and
     * <tt>lafProperties</tt> are <code>null</code>. If only <tt>installdir</tt>
     * is <code>null</code> the parent directory of <tt>lafProperties</tt> will
     * be used as the installation target.<p>
     *
     * If <tt>lafProperties</tt> is not <code>null</code> information about the
     * installed LookAndFeels will be read from both
     * <tt>lib/swing.properties</tt> and <tt>lafProperties</tt> and saved to
     * <tt>lib/swing.properties</tt> if possible and <tt>lafProperties</tt>
     * otherwise.<p>
     *
     * In addition a custom class loader can be specified, which will be used
     * exclusively to instantiate LookAndFeel classes. A suitable classloader
     * can be created using a {@link
     * de.mospace.lang.DefaultClassLoaderProvider ClassLoaderProvider}.
     * This is necessary whenever newly
     * installed jar files are not added to your classpath by some other
     * mechanism outside this class.<p>
     *
     * If you save the current LookAndFeel to Preferences your Application
     * should call {@link LookAndFeelMenu#setLookAndFeel(Preferences pref,
     * ClassLoader loader)} before creating its GUI.
     *
     * @param pref the preference node from which to read and to which to save
     *      the current LookAndFeel. If it is <code>null</code> the current
     *      LookAndFeel will be forgotten when the JVM exits.
     * @param w the window whose LookAndFeel this LookAndFeelMenu controls. Can
     *      be <code>null</code>.
     * @param lafProperties a fallback properties file. Can be <code>null</code>.
     * @param installdir a fallback installation directory. Can be <code>null</code>.
     * @param customCL Description of the Parameter
     */
    public LookAndFeelMenu(Preferences pref, Window w,
            File lafProperties, File installdir, ClassLoader customCL) {
        LookAndFeelInfo[] looks1 = UIManager.getInstalledLookAndFeels();
        LookAndFeelInfo[] looks2 = readProperties(lafProperties);
        LookAndFeelInfo[] looks;
        if (looks2.length == 0) {
            looks = looks1;
        } else if (looks1.length == 0) {
            looks = looks2;
        } else {
            Set looksSet = new TreeSet(
                new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return
                                ((LookAndFeelInfo) o1).getName().compareTo(
                                ((LookAndFeelInfo) o2).getName());
                    }
                });
            looksSet.addAll(Arrays.asList(looks1));
            looksSet.addAll(Arrays.asList(looks2));
            looks = (LookAndFeelInfo[]) looksSet.toArray(
                    new LookAndFeelInfo[looksSet.size()]);
        }
        win = w;
        node = pref;
        if(customCL != null){
            cl = customCL;
        }
        init(looks, lafProperties, (lafProperties != null && installdir == null)
                 ? lafProperties.getAbsoluteFile().getParentFile()
                 : installdir);
    }

    public LookAndFeelMenu(Preferences pref, Window w,
            File lafProperties, File installdir, ClassLoaderProvider clp) {
        this(pref, w, lafProperties, installdir, clp.getClassLoader());
        this.clp = clp;
    }


    private void init(LookAndFeelInfo[] looks, final File lafProperties,
            final File installdir) {
        lafItems = new Vector(looks.length);
        setText("LnF");
        setMnemonic(KeyEvent.VK_L);

        JMenuItem installLaF = new LafInstallerMenuItem(lafProperties, installdir);
        add(installLaF);

        Arrays.sort(looks,
            new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((LookAndFeelInfo) o1).getName().compareTo(((LookAndFeelInfo) o2).getName());
                }
            });

        String currentLNF = currentLaF();
        for (int k = 0; k < looks.length; k++) {
            LaFMenuItem item = addLookAndFeel(looks[k]);
            if (item.getLaFClassName().equals(currentLNF)) {
                item.setSelected(true);
            }
            item.addActionListener(lnfListener);
        }

        if (node != null) {
            String prefLaF = node.get("LookAndFeelClass", UIManager.getSystemLookAndFeelClassName());

            /*
             *  Select and switch to prefLaF
             */
            int selectedIndex = lafItems.indexOf(new LaFMenuItem("", prefLaF));
            if (selectedIndex != -1) {
                switchLaF((LaFMenuItem) lafItems.get(selectedIndex));
            }
        }
        UIManager.addPropertyChangeListener(LaFListener);
    }


    private LaFMenuItem addLookAndFeel(LookAndFeelInfo laf) {
        LaFMenuItem item = new LaFMenuItem(laf);
        lafItems.add(item);
        bg.add(item);
        add(item);
        return item;
    }


    private String currentLaF() {
        return UIManager.getLookAndFeel().getClass().getName();
    }

    public void setDialogParent(Component p){
        dialogParent = p;
    }


    private void switchLaF(LaFMenuItem item) {
        /*
         *  get selected look and feel class name
         */
        String lafClass = item.getLaFClassName();

        /*
         *  if it is not the current LaF try to switch
         */
        if (lafClass.equals(currentLaF())){
                return;
        }
        if(LookAndFeelMenu.setLookAndFeel(lafClass, cl)) {

            /*
            *  if we did indeed switch update GUI and prefs
            */
            updateApplicationGui();
            if (node != null) {
                save();
            }
        } else {
            item.setSelected(false);
            item.setEnabled(false);
        }
    }


    private LookAndFeelInfo[] readProperties(File f) {
        Properties p = new Properties();
        if (f.isFile()) {
            InputStream fips = null;
            try {
                fips = new BufferedInputStream(new FileInputStream(f));
                p.load(fips);
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
        String silafs = p.getProperty("swing.installedlafs");
        LookAndFeelInfo[] result = new LookAndFeelInfo[0];
        if (silafs != null) {
            String[] lafs = silafs.split("\\s*,\\s*");
            result = new LookAndFeelInfo[lafs.length];
            for (int i = 0; i < lafs.length; i++) {
                result[i] = new LookAndFeelInfo(
                        p.getProperty("swing.installedlaf." + lafs[i] + ".name", lafs[i]),
                        p.getProperty("swing.installedlaf." + lafs[i] + ".class"));
            }
        }
        return result;
    }


    /**
     * Constructs a new LookAndFeel object, calls
     * UIManager.setLookAndFeel(LookAndFeel laf), and consumes all eventual
     * exceptions. This method should be used by subclasses of LookAndFeelMenu
     * only.
     *
     * @param classname the classname of the new LookAndFeel
     * @return false if an exception occurred true otherwise
     * @see UIManager#setLookAndFeel(LookAndFeel laf)
     */
    protected boolean setLookAndFeel(String classname) {
        return setLookAndFeel(classname, cl);
    }


    /**
     * Tries to create an instance of the specified LookAndFeel class using the
     * provided ClassLoader and calls UIManager.setLookAndFeel(LookAndFeel laf).
     * Consumes all eventual exceptions.<p>
     * This method should be used from outside this class and its subclasses
     * only when no GUI components and in particular no instances of this
     * class have been created.
     *
     * @param classname the classname of the new LookAndFeel
     * @param loader the classloader that will be used to create an instance of
     * the LookAndFeel class. If loader is <code>null<code> the system class
     * loader will be used instead.
     * @return false if an exception occurred true otherwise
     * @see UIManager#setLookAndFeel(LookAndFeel laf)
     */
    public static boolean setLookAndFeel(String classname, ClassLoader loader) {
        boolean result = true;
        LookAndFeel oldLAF = UIManager.getLookAndFeel();
        try {
            LookAndFeel newLAF = (LookAndFeel)
                    Class.forName(classname,
                                  true,
                                  (loader == null)
                                    ? ClassLoader.getSystemClassLoader()
                                    : loader
                    ).newInstance();
            UIManager.setLookAndFeel(newLAF);
        } catch (Exception ex) {
            System.err.println("Error setting Look and Feel to " + classname);
            try{
                UIManager.setLookAndFeel(oldLAF);
                System.err.println("Successfully switched back to " + oldLAF.toString());
            } catch (Exception ignore){
                System.err.println(ignore);
                System.err.flush();
            }
            result = false;
        }
        return result;
    }


    /**
     * Tries to load a previously saved current LookAndFeel. If none is found
     * {@link UIManager#getSystemLookAndFeelClassName()} is used as a default.
     * Consumes all eventual exceptions.<p>
     * This method should be used from outside this class and its subclasses
     * only when no GUI components and in particular no instances of this
     * class have been created.
     *
     * @param pref the Preferences node to load the LookAndFeel from
     * @param loader the classloader that will be used to create an instance of
     * the LookAndFeel class. If loader is <code>null<code> the system class
     * loader will be used instead.
     * @return false if an exception occurred true otherwise
     * @see UIManager#setLookAndFeel(LookAndFeel laf)
     */
    public static boolean setLookAndFeel(Preferences pref, ClassLoader loader) {
        String lnf = pref.get("LookAndFeelClass",
                UIManager.getSystemLookAndFeelClassName());
        boolean result = setLookAndFeel(lnf, loader);
        if(!result){
            pref.remove("LookAndFeelClass");
        }
        return result;
    }


    /**
     * The default implementation updates the window provided to the
     * constructor. Override this method according to your application-specific
     * needs.
     */
    protected void updateApplicationGui() {
        if (win != null) {
            SwingUtilities.updateComponentTreeUI(win);
            win.validate();
        }
    }

    private void save() {
        node.put("LookAndFeelClass",
                currentLaF());
    }


    /**
     * Description of the Class
     *
     * @author ringler
     */
    private static class LaFMenuItem extends JRadioButtonMenuItem {
        private final LookAndFeelInfo lafInfo;


        /**
         * Constructs a new LaFMenuItem.
         *
         * @param info Description of the Parameter
         */
        public LaFMenuItem(LookAndFeelInfo info) {
            super(info.getName());
            lafInfo = info;
        }


        /**
         * Constructs a new LaFMenuItem.
         *
         * @param name Description of the Parameter
         * @param classname Description of the Parameter
         */
        public LaFMenuItem(String name, String classname) {
            super(name);
            lafInfo = new LookAndFeelInfo(name, classname);
        }


        /**
         * Gets the lookandFeelInfo attribute of the LaFMenuItem object
         *
         * @return The lookandFeelInfo value
         */
        public LookAndFeelInfo getLookandFeelInfo() {
            return lafInfo;
        }


        /**
         * Gets the laFClassName attribute of the LaFMenuItem object
         *
         * @return The laFClassName value
         */
        public String getLaFClassName() {
            return getLookandFeelInfo().getClassName();
        }


        /**
         * Description of the Method
         *
         * @param o Description of the Parameter
         * @return Description of the Return Value
         */
        public boolean equals(Object o) {
            return (o instanceof LaFMenuItem) &&
                    (((LaFMenuItem) o).getLaFClassName().equals(getLaFClassName()));
        }


        /**
         * Description of the Method
         *
         * @return Description of the Return Value
         */
        public int hashCode() {
            return getLaFClassName().hashCode();
        }
    }


    /**
     * Description of the Class
     *
     * @author ringler
     */
    private class LafInstallerMenuItem extends JMenuItem implements ActionListener {
        private final File idir;
        private final File iprops;


        /**
         * Constructs a new LafInstallerMenuItem.
         *
         * @param lafProperties Description of the Parameter
         * @param installDir Description of the Parameter
         */
        public LafInstallerMenuItem(File lafProperties, File installDir) {
            super(GLOBALS.getString("LAF_MENU_INST_NEW"));
            idir = installDir;
            iprops = lafProperties;
            addActionListener(this);
        }


        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            Component parent = (dialogParent == null)? win : dialogParent;
            if (LaFInstaller.canInstall(iprops)) {
                try {
                    LaFInstaller lafi = new LaFInstaller(
                            parent,
                            idir,
                            iprops);
                    LaFInstaller.LaFPackage laf = lafi.queryLaF();
                    if (laf == null){
                        System.err.println("No Look and Feel found.");
                    } else {
                        System.err.print(laf);
                        if(lafi.installLaF(laf, clp)) {
                            JMenuItem item = addLookAndFeel(laf);
                            if(clp == null){
                                item.setEnabled(false);
                            } else {
                                cl = clp.getClassLoader();
                                item.addActionListener(lnfListener);
                                item.doClick();
                            }
                            System.err.println(" installed");
                        } else {
                            System.err.println(" not installed.");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                LaFInstaller.warnCannotInstall(parent, iprops);
            }
            System.err.flush();
            System.out.flush();
        }
    }

}
