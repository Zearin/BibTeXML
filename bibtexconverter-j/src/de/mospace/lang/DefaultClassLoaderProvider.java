package de.mospace.lang;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/** A fully functional implementation of the {@link ClassLoaderProvider}
* interface. Libraries can be registered with a DefaultClassLoaderProvider
* and class loaders that look for class files in these libraries
* can be obtained with the {@link #getClassLoader} method. **/ 
public class DefaultClassLoaderProvider extends MinimalClassLoaderProvider implements Serializable{
    /** serialization version id */
    public static final long serialVersionUID = -7115012927531022705L;
    /** The parent class loader for class loaders returned by future
    * calls to {@link #getClassLoader}.
    */
    private transient ClassLoader classloader; //we don't want to store the class loader itself
    
    /** A set of URLs containing the jar files and directories in the class
    * path of the last class loader. */
    private final Set jars = new HashSet();
    
    /** A set of URLs containing newly registered jar files and directories. */
    private final Set newJars = new HashSet(); //can't make this transient because it's final

    /** Constructs a new ClassLoaderProvider whose {@link #getClassLoader}
     * method will return class loaders that finally delegate to
     * the system class loader.
     **/
    public DefaultClassLoaderProvider(){
        classloader = ClassLoader.getSystemClassLoader();
    }

    /** Constructs a new ClassLoaderProvider whose {@link #getClassLoader}
     * method will return class loaders that finally delegate to
     * the specified class loader.
     * @param parent the delegating-parent for all class loaders returned
     * by this ClassLoaderProvider's {@link #getClassLoader} method, may be
     * <code>null</code>.
     **/
    public DefaultClassLoaderProvider(ClassLoader parent){
        classloader = parent;
    }
    
    /** Tries to determine the directory that is or that contains the 
    * class path element from which the specified class has been loaded.
    * @param klass a class loaded from the local file system
    * @throws IllegalArgumentException if the repository root of the 
    * specified class is not a file.
    */
    public static File getRepositoryRootDir(Class klass){
        File f = new File(getRepositoryRoot(klass));
        return (f == null || f.isDirectory())? f : f.getAbsoluteFile().getParentFile();
    }
    
    /** Tries to determine the class path
    * element from which the specified class has been loaded.
    * @param klass a non-null class object
    * @return the URI of the class path element from which <code>klass</code>
    * has been loaded
    **/
    public static URI getRepositoryRoot(Class klass){
        /* Constructs the resource name for this Class by replacing
          dots with slashes, prepending a slash, and appending the
          .class extension */
        String resourceName =
                "/" + klass.getName().replaceAll("\\.","/") + ".class";
        /* gets the resource for the class resource name as an url */
        URL classResource = klass.getResource(resourceName);
        /* deletes the resource name from the class resource url
        * and thus extracts the class repository url
        */
        String s_resource = classResource.toString().replaceFirst("\\Q"+resourceName+"\\E$","");
        /* deletes a leading "jar:" as well as the first occurrance of "!$"
        * from the class repository url */
        s_resource = s_resource.replaceFirst("^jar:","").replaceFirst("!$","");
        ///* extracts the directory part of the class repository url */
        //s_resource = s_resource.substring(0,s_resource.lastIndexOf("/"));
        URI result = null;
        try{
            /* tries to construct a new File for the directory part of the
            * class repository url */
            result = new URI(s_resource);
        } catch (URISyntaxException ignore){
        }
        return result;
    }

    /** Determines whether the path of the specified file starts with
     * the directory specified in the Java system property
     * java.io.tmpdir.
     * @param file a non-null file object
     * @return whether the specified file resides in the java temporary
     * directory or one of its sub-directories
     * @see java.lang.System#getProperty
     **/
    public static boolean isTemporary(File file){
        boolean result = false;
        try{
            String tempdir = System.getProperty("java.io.tmpdir");
            if(tempdir != null && tempdir.length() != 0){
                tempdir = (new File(tempdir)).getCanonicalPath();
                result = file.getCanonicalPath().startsWith(tempdir);
            }
        } catch (IOException ignore){}
        return result;
    }

    /** Returns all libraries that have been registered with this
     * ClassLoaderProvider, so far.
     * @return an array containing the URLs of all libraries registered with
     * this ClassLoaderProvider.
     **/
    public final synchronized URL[] getLibraries(){
        updateClassLoader();
        return (URL[]) jars.toArray(new URL[jars.size()]);
    }

    /** Returns the library files that have been registered with this
     * ClassLoaderProvider, so far.
     * @return an array containing the library files registered with
     * this ClassLoaderProvider.**/
    public final synchronized File[] getLibraryFiles(){
        URL[] urls = getLibraries();
        List jarFiles = new Vector(urls.length);
        for(int i=0; i<urls.length; i++){
            try{
                jarFiles.add(new File(new URI(urls[i].toString())));
            } catch (URISyntaxException ex){
                //Should never happen
                ex.printStackTrace();
            }
        }
        return (File[]) jarFiles.toArray(new File[jarFiles.size()]);
    }

    /** Returns a class loader that looks for class files in all libraries
     * that have been successfully registered with this ClassLoaderProvider,
     * so far.
     * Subsequent calls to this method will return the same class loader
     * or class loaders that delegate to all class loaders previously returned.
     * @return a ClassLoader that looks for class files in all successfully
     * registered libraries.
     */
    public final synchronized ClassLoader getClassLoader(){
        return updateClassLoader();
    }

    /** Requests that the specified jar file be registered, so that it is
     * in the class path of all class loaders returned by future calls to
     * {@link #getClassLoader}.
     * @param jarFile a jar archive with Java class files 
     * @return true if the library had not been registered before and 
     *         was successfully registered with this ClassLaoderProvider.
     * @throws IllegalArgumentException if jarFile does not exist
     *         or is not a jar file
     **/
    public final synchronized boolean registerLibrary(File jarFile){
        super.registerLibrary(jarFile); //will throw the required Exceptions
        return addLib(jarFile);
    }

    /**  Requests that the specified jar files be registered, so that they are
     * in the class path of all class loaders returned by future calls to
     * {@link #getClassLoader}.
     * @param jarFiles an array of jar archives with Java class files 
     * @return true if at least one jarFile was newly registered
     **/
    public final synchronized boolean registerLibraries(File[] jarFiles){
        boolean result = false;
        for(int i = 0; i<jarFiles.length; i++){
            result = addLib(jarFiles[i]) || result;
        }
        return result;
    }

    /**  Requests that all jar files in the specified directory be
     * registered, so that they are in the class path of all class loaders
     * returned by future calls to
     * {@link #getClassLoader}.
     * @param dir a directory with jar archives. The client must assure that
     * dir exists and is a directory. dir may contain non-jar files
     * @return true if there was at least one jar archive in <code>dir</code>,
     * for wich the request was honored.
     * @throws IllegalArgumentException if <code>dir</code> is not a directory
     */
    public final synchronized  boolean registerLibraryDirectory(File dir){
        super.registerLibraryDirectory(dir); //will throw the right Exceptions
        return addLibDir(dir);
    }

    /**  Requests that all jar files in the specified directories be
     * registered, so that they are in the class path of all class loaders
     * returned by future calls to {@link #getClassLoader}.
     * Non-directory files in <code>dirs</code> will be ignored.
     * @param dirs an array of directories with jar archives
     * @return true if there was at least one jar archive in <code>dirs</code>,
     * for wich the request was honored.
     **/
    public final synchronized boolean registerLibraryDirectories(File[] dirs){
        boolean result = false;
        for(int i=0; i < dirs.length; i++){
            if(dirs[i].isDirectory()){
                result = registerLibraryDirectory(dirs[i]) || result;
            }
        }
        return result;
    }

    /**  Requests that the specified directory be
     * registered, so that it is on the class path of all class loaders
     * returned by future calls to {@link #getClassLoader}.
     * @param dir a non-null directory with Java class files
     * @return true if the directory was newly registered
     **/
    public final synchronized boolean registerDirectory(File dir){
        boolean result = false;
        if (dir.isDirectory()){
            try{
                URL newURL = dir.toURI().toURL();
                if(! jars.contains(newURL) ){
                    //System.out.println("adding " + newURL);
                    result = newJars.add(newURL);
                }
            } catch (MalformedURLException ex){
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
        return result;
    }

    /** Requests that the specified jar file be registered, so that it is
     * in the class path of all class loaders returned by future calls to
     * {@link #getClassLoader}.
     * @param jarFile a jar archive with Java class files 
     * @return true if the library had not been registered before and 
     *         was successfully registered with this ClassLaoderProvider.
     **/
    private final boolean addLib(File jar){
        boolean result = false;
        if (jar.isFile() && jar.getName().endsWith(".jar")){
            try{
                URL newURL = jar.toURI().toURL();
                if(! jars.contains(newURL) ){
                    //System.out.println("adding " + newURL);
                    result = newJars.add(newURL);
                }
            } catch (MalformedURLException ex){
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
        return result;
    }

    /**  Requests that all jar files in the specified directory be
     * registered, so that they are in the class path of all class loaders
     * returned by future calls to
     * {@link #getClassLoader}.
     * @param dir a directory with jar archives. The client must assure that
     * dir exists and is a directory. dir may contain non-jar files
     * @return true if there was at least one jar archive in
     * <code>libDir</code>,  for wich the request was honored.
     */
    private final boolean addLibDir(File libDir){
        boolean result = false;
        if(libDir.isDirectory()){
            File[] libs = libDir.listFiles();
            for(int i=0; i<libs.length; i++){
                result = addLib(libs[i]) || result;
            }
        }
        return result;
    }

    /** Updates the classloader member if new libraries have been
     * registered.
     */
    private final ClassLoader updateClassLoader(){
        if (newJars.size() != 0){
            classloader = new URLClassLoader(
                    (URL[]) newJars.toArray(new URL[newJars.size()]), classloader );
            jars.addAll(newJars);
            newJars.clear();
            //System.out.println(Arrays.asList(((URLClassLoader) classloader).getURLs()));
        }
        return classloader;
    }
    
    /** Serialization. */
    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException{
        updateClassLoader();
        out.defaultWriteObject();
    }
    
    /** De-serialization. */
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        if(jars.size() != 0){
            classloader = new URLClassLoader(
                    (URL[]) newJars.toArray(new URL[newJars.size()]), classloader );
        }
    }
}
