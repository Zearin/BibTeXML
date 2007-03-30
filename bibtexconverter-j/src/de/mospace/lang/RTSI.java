package de.mospace.lang;

/**
* RTSI.java
*
* Created: Wed Jan 24 11:15:02 2001
*
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;

/**
* This utility class is looking for all the public classes
* implementing or inheriting from a given interface or class that
* can be instantiated with a zero-argument constructor.
* (RunTime Subclass Identification)
*
* @author <a href="mailto:daniel@satlive.org">Daniel Le Berre</a>
* @version 1.0
*
* [MR] find(String,Class) returns array with class names instead of printing to stdout.
* [MR] Using URIs to look up resources (previous version did not work
* when there was a blank in the file name)
* [MR] specify class loader to use
* [MR] optionally use properties cache
*/
public class RTSI {
    private static Logger logger = Logger.getLogger("de.mospace.lang");
    static{
        logger.setUseParentHandlers(false);
    }
    private final ClassLoader classloader;

    /** deprecated use a ClassLoaderProvider instead */
    public static ClassLoader createLibClassLoader(File libdir){
        return createLibClassLoader(new File[]{libdir});
    }

    /** deprecated use a ClassLoaderProvider instead */
    public static ClassLoader createLibClassLoader(File[] libdir){
        ClassLoader libClassLoader = null;
        List jars = new Vector(libdir.length * 10);
        for(int j = 0; j<libdir.length; j++){
            File lib = libdir[j];
            if(lib.isDirectory()){
                File[] libs = lib.listFiles();

                for(int i=0; i<libs.length; i++){
                    if(libs[i].isFile() && libs[i].getName().endsWith(".jar")){
                        try{
                            jars.add(libs[i].toURI().toURL());
                        } catch (MalformedURLException ignore){
                        }
                    }
                }
            } else if(lib.isFile()){
                if(lib.getName().endsWith(".jar")){
                    try{
                        jars.add(lib.toURI().toURL());
                    } catch (MalformedURLException ignore){
                    }
                }
            }
        }
        libClassLoader = (jars.size() >0)
            ? new URLClassLoader((URL[]) jars.toArray(new URL[jars.size()]),
                    ClassLoader.getSystemClassLoader())
            : ClassLoader.getSystemClassLoader();
        return libClassLoader;
    }

    public RTSI(){
        this(ClassLoader.getSystemClassLoader());
    }

    public RTSI(ClassLoader cl){
        classloader = cl;
    }

    public ClassLoader getClassLoader(){
        return classloader;
    }

    public static void addLogHandler(Handler lh){
        logger.setLevel(lh.getLevel());
        logger.addHandler(lh);
    }

    /**
    * Display all the classes inheriting or implementing a given
    * class in the currently loaded packages.
    * Works only with the default class loader!!!
    * @param tosubclassname the name of the class to inherit from
    */
    public void find(String tosubclassname) {
        try {
            Class tosubclass = Class.forName(tosubclassname, true, classloader);
            Package [] pcks = Package.getPackages();
            for (int i=0;i<pcks.length;i++) {
                find(pcks[i].getName(),tosubclass);
            }
        } catch (ClassNotFoundException ex) {
            logger.logp(Level.SEVERE,
                RTSI.class.getName(),
                "find(String)",
                "Class "+tosubclassname+" not found!", ex);
            System.err.println("Class "+tosubclassname+" not found!");
        }
    }

    /**
    * Display all the classes inheriting or implementing a given
    * class in a given package.
    * @param pckname the fully qualified name of the package
    * @param tosubclassname the name of the class to inherit from
    */
    public void find(String pckname, String tosubclassname) {
        try {
            Class tosubclass = Class.forName(tosubclassname, true, classloader);
            find(pckname,tosubclass);
        } catch (ClassNotFoundException ex) {
            logger.logp(Level.SEVERE,
                    RTSI.class.getName(),
                    "find(String, String)",
                    "Class "+tosubclassname+" not found!", ex);
            System.err.println("Class "+tosubclassname+" not found!");
        }
    }

    /** pref cache **/
    public String[] find(String pckname, Class tosubclass,
            Preferences pref, String buildprop, String buildval){
        String[] result = null;
        Preferences p = pref;
        if (p != null){
            int reslength = pref.getInt(pckname, -1);
            if(reslength < 0){
                p = null;
            } else {
                result = new String[reslength];

                /* check whether this is a new build */
                if(p.get(buildprop, null) != null && buildval.equals(p.get(buildprop, null))){
                    /* if not try loading saved values */
                    for(int i=0; i<reslength; i++){
                        result[i] = p.get(pckname + "." + i, null);
                        if(result[i] == null){
                            /* the prefs are not what we expect them to be */
                            p = null;
                            break;
                        }
                    }
                } else {
                    p = null;
                }
            }
        }
        if(p == null){
            result = find(pckname, tosubclass);
            pref.putInt(pckname, result.length);
            for(int i=0; i<result.length; i++){
                pref.put(pckname+"."+i, String.valueOf(result[i]));
            }
            pref.put(buildprop, buildval);
        }
        return result;
    }

    /** property cache **/
    public String[] find(String pckname, Class tosubclass,
            File configdir, String buildprop, String buildval){
        File prop = new File(configdir, tosubclass.getName()+".properties");
        Properties properties = new Properties();
        prop.getAbsoluteFile().getParentFile().mkdirs();
        boolean fromFile = prop.isFile();
        String[] result = null;
        if (fromFile){
            InputStream fips = null;
            try{
                fips = new BufferedInputStream(new FileInputStream(prop));
                properties.load(fips);
                int reslength = Integer.parseInt(properties.getProperty(pckname));
                result = new String[reslength];

                /* check whether this is a new build */
                if(properties.get(buildprop) != null &&
                    buildval.equals(properties.get(buildprop)))
                {
                    /* if not try loading saved values */
                    for(int i=0; i<reslength; i++){
                        result[i] = properties.getProperty(pckname + "." + i);
                        if(result[i] == null){
                            /* the property file is not what we expect it to be */
                            fromFile = false;
                            break;
                        }
                    }
                } else {
                    fromFile = false;
                }
            } catch (IOException ex){
                fromFile = false;
            } catch (IllegalArgumentException ex){
                /* property file is malformed */
                fromFile = false;
            } finally {
                if(fips != null){
                    try{
                        fips.close();
                    } catch (IOException ioex){
                        ioex.printStackTrace();
                    }
                }
            }
        }
        if(!fromFile){
            result = find(pckname, tosubclass);
            OutputStream fops = null;
            try{
                fops = new BufferedOutputStream(new FileOutputStream(prop));
                properties.setProperty(pckname, String.valueOf(result.length));
                for(int i=0; i<result.length; i++){
                    properties.setProperty(pckname+"."+i,
                            String.valueOf(result[i]));
                }
                properties.setProperty(buildprop, buildval);
                properties.store(fops,
                "This file caches which implementations of " +
                tosubclass.getName() +
                " are available. Delete it when you want to rebuild the cache.");
            } catch (IOException ioex){
                ioex.printStackTrace();
            } finally {
                if(fops != null){
                    try{
                        fops.close();
                    } catch (IOException ioex){
                        ioex.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    /**
    * Display all the classes inheriting or implementing a given
    * class in a given package.
    * @param pckgname the fully qualified name of the package
    * @param tosubclass the Class object to inherit from
    */
    public String[] find(String pckgname, Class tosubclass) {
        logger.logp(Level.INFO,
                    RTSI.class.getName(),
                    "find(String, Class)",
                    "Searching for " + tosubclass.getName() + " in package " + pckgname);
        // Code from JWhich
        // ======
        // Translate the package name into an absolute path
        String name = new String(pckgname);
        if (!name.startsWith("/")) {
            //name = "/" + name;
        }
        name = name.replace('.','/');

        // Get a File object for the package
        URL url = classloader.getResource(name);
        // URL url = tosubclass.getResource(name);
        // URL url = ClassLoader.getSystemClassLoader().getResource(name);
        //System.out.println(name+"->"+url);

        // Happens only if the jar file is not well constructed, i.e.
        // if the directories do not appear alone in the jar file like here:
        //
        //          meta-inf/
        //          meta-inf/manifest.mf
        //          commands/                  <== IMPORTANT
        //          commands/Command.class
        //          commands/DoorClose.class
        //          commands/DoorLock.class
        //          commands/DoorOpen.class
        //          commands/LightOff.class
        //          commands/LightOn.class
        //          RTSI.class
        //
        if (url==null) return new String[]{};


        File directory = null;
        try {
            URI u = new URI(url.toString()); //Java 1.4 version of url.ToURI()
            //test if u is hierarchical
            if (!u.isOpaque()){
                directory  = new File(u);
            }
        } catch (java.net.URISyntaxException ex){
            logger.logp(Level.SEVERE,
                    RTSI.class.getName(),
                    "find(String, Class)",
                    "Error in RTSI: Returning Empty Array!", ex);
            System.err.println("Error in RTSI: Returning Empty Array!");
            ex.printStackTrace(System.err);
            return new String[]{};
        }
        // New code
        // ======
        Vector validSubclasses = new Vector();
        if (directory != null && directory.exists()) {
            logger.logp(Level.INFO,
                    RTSI.class.getName(),
                    "find(String, Class)",
                    "Searching for "+ tosubclass.getName() +
                    " in directory " + directory.getAbsolutePath());
            // Get the list of the files contained in the package
            String [] files = directory.list();
            for (int i=0;i<files.length;i++) {

                // we are only interested in .class files
                if (files[i].endsWith(".class")) {
                    // removes the .class extension
                    String classname = files[i].substring(0,files[i].length()-6);
                    classname = pckgname+"."+classname;
                    if(testClass(classname, tosubclass)){
                        validSubclasses.add(classname);
                    }
                }
            }
        } else {
            logger.logp(Level.INFO,
                    RTSI.class.getName(),
                    "find(String, Class)",
                    "Searching for " + tosubclass.getName() + " in jarfile. ");
            try {
                // It does not work with the filesystem: we must
                // be in the case of a package contained in a jar file.
                JarURLConnection conn = (JarURLConnection)url.openConnection();
                String starts = conn.getEntryName();
                JarFile jfile = conn.getJarFile();
                logger.logp(Level.INFO,
                        RTSI.class.getName(),
                        "find(String, Class)",
                        "Jarfile name: " + jfile.getName());
                Enumeration e = jfile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry)e.nextElement();
                    String entryname = entry.getName();
                    if (entryname.startsWith(starts)
                        &&(entryname.lastIndexOf('/')<=starts.length())
                    &&entryname.endsWith(".class")) {
                        String classname = entryname.substring(0,entryname.length()-6);
                        if (classname.startsWith("/")){
                            classname = classname.substring(1);
                        }
                        classname = classname.replace('/','.');
                        if(testClass(classname, tosubclass)){
                            validSubclasses.add(classname);
                        }
                    }
                }
            } catch (IOException ioex) {
                System.err.println(ioex);
            }
        }
        return (String[]) validSubclasses.toArray(new String[validSubclasses.size()]);
    }

    public void main(String []args) {
        if (args.length==2) {
            find(args[0],args[1]);
        } else {
            if (args.length==1) {
                find(args[0]);
            } else {
                System.out.println("Usage: java RTSI [<package>] <subclass>");
            }
        }
    }

    public boolean testClass(String classname, Class tosubclass){
        boolean result = false;
        logger.logp(Level.FINE,
        RTSI.class.getName(),
        "testClass",
        "Candidate class: " + classname);
        try {
            // Test if this class is public.
            Class cl = Class.forName(classname, true, classloader);
            if(!Modifier.isPublic(cl.getModifiers())){
                logger.logp(Level.FINE,
                RTSI.class.getName(),
                "testClass",
                "Candidate class: " + classname +
                "; Class is not public!");
            } else {
                // Try to create an instance of the object
                Object o = Class.forName(classname, true, classloader).newInstance();
                if (tosubclass.isInstance(o)) {
                    logger.logp(Level.FINE,
                    RTSI.class.getName(),
                    "testClass",
                    "Candidate class " + classname +
                    " passed tests.");
                    result = true;
                } else {
                    logger.logp(Level.FINE,
                    RTSI.class.getName(),
                    "testClass",
                    "Candidate class " + classname +
                    " is not an instance of " + tosubclass.getName() +".");
                }
            }
        } catch (NoClassDefFoundError ncdfe){
            logger.logp(Level.FINE,
            RTSI.class.getName(),
            "testClass",
            "Candidate class: " + classname +
            "; Class Definition not found!");
            System.err.println(ncdfe);
        } catch (ClassNotFoundException cnfex) {
            logger.logp(Level.FINE,
            RTSI.class.getName(),
            "testClass",
            "Candidate class: " + classname +
            "; Class not found!");
            System.err.println(cnfex);
        } catch (InstantiationException iex) {
            // We try to instanciate an interface
            // or an object that does not have a
            // default constructor
            logger.logp(Level.FINE,
            RTSI.class.getName(),
            "testClass",
            "Candidate class: " + classname +
            "; Class is not instantiable or does not have" +
            " a default constructor!");
        } catch (IllegalAccessException iaex) {
            // The class is not public
            logger.logp(Level.FINE,
            RTSI.class.getName(),
            "testClass",
            "Candidate class: " + classname +
            "; Class is not accessible!");
        }
        return result;
    }
}// RTSI
