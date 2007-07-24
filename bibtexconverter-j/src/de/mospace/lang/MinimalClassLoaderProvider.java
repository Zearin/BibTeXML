package de.mospace.lang;
/* Mp3dings - manage mp3 meta-information
 * Copyright (C) 2003 Moritz Ringler
 * $Id$
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
import java.io.File;
import java.io.Serializable;

/** A minimal implementation of the ClassLoaderProvider interface whose
* {@link #getClassLoader getClassLoader} method always returns the same classloader. **/
public class MinimalClassLoaderProvider implements ClassLoaderProvider, Serializable {
    private final ClassLoader classloader;

    /** Contructs a new MinimalClassLoaderProvider whose {@link #getClassLoader}
    * method will always return the specified class loader.
    * @param classLoader the class loader that will be returned by this object's
    * {@link #getClassLoader} method
    */
    public MinimalClassLoaderProvider(ClassLoader classLoader){
        classloader = classLoader;
    }

    /** Contructs a new MinimalClassLoaderProvider whose {@link #getClassLoader}
    * method will always return the system class loader.
    * @see java.lang.ClassLoader#getSystemClassLoader()
    */
    public MinimalClassLoaderProvider(){
        classloader = ClassLoader.getSystemClassLoader();
    }

    /** Returns the class loader specified when this MinimalClassLoader
    * is instantiated. */
    public ClassLoader getClassLoader(){
        return classloader;
    }

     /** Apart from possibly throwing an IllegalArgumentException a call
     * to this method has no effect.
     * @param jarFile a jar archive with Java class files 
     * @return <code>false</code>
     * @throws IllegalArgumentException if jarFile does not exist
     *         or is not a jar file
     **/
    public boolean registerLibrary(File jarFile){
        if (! (jarFile.isFile() && jarFile.getName().endsWith(".jar"))){
            throw new IllegalArgumentException("Argument must be a jar file.");
        }
        return false;
    }

    /** Does nothing.
     * @param jarFiles an array of jar archives with Java class files 
     * @return <code>false</code> */
    public boolean registerLibraries(File[] jarFiles){
        return false;
    }

     /** Apart from possibly throwing an IllegalArgumentException a call
     * to this method has no effect.
     * @param dir a directory with jar archives
     * @return <code>false</code>
     * @throws IllegalArgumentException if <code>dir</code> is not a directory
     **/
    public boolean registerLibraryDirectory(File dir){
        if (! dir.isDirectory()){
            throw new IllegalArgumentException("Argument must be a directory.");
        }
        return false;
    }

    /** Does nothing.
     * @param dirs an array o directories with jar archives
     * @return <code>false</code> */
    public boolean registerLibraryDirectories(File[] dirs){
        return false;
    }
}
