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

/** This interface essentially provides the functionality of a
* dynamic class path. Libraries can be registered with a ClassLoaderProvider
* and class loaders that look for class files in these libraries
* can be obtained with the {@link #getClassLoader} method.
**/
public interface ClassLoaderProvider extends Serializable{
    /** serialization version id */
    public static final long serialVersionUID = 372109672623762879L;
    
    /** Returns a class loader that looks for class files in all libraries
     * that have been successfully registered with this ClassLoaderProvider,
     * so far.
     * Subsequent calls to this method must return the same class loader
     * or class loaders that delegate to all class loaders previously returned.
     * @return a ClassLoader that looks for class files in all successfully
     * registered libraries.
     */
    public ClassLoader getClassLoader();

    /** Requests that the specified jar file be registered, so that it is
     * in the class path of all class loaders returned by future calls to
     * {@link #getClassLoader}.
     * @param jarFile a jar archive with Java class files 
     * @return true if the ClassLoaderProvider honored the request.
     * @throws IllegalArgumentException if jarFile does not exist
     *         or is not a jar file
     **/
    public boolean registerLibrary(File jarFile);

    /**  Requests that the specified jar files be registered, so that they are
     * in the class path of all class loaders returned by future calls to
     * {@link #getClassLoader}.
     * @param jarFiles an array of jar archives with Java class files 
     * @return true if the ClassLoaderProvider honored the request at least for
     * one jar file.
     **/
    public boolean registerLibraries(File[] jarFiles);

    /**  Requests that the all jar files in the specified directory be
     * registered, so that they are in the class path of all class loaders
     * returned by future calls to
     * {@link #getClassLoader}.
     * @param dir a directory with jar archives
     * @return true if there was at least one jar archive in <code>dir</code>,
     * for wich the request was honored.
     **/
    public boolean registerLibraryDirectory(File dir);

    /**  Requests that the all jar files in the specified directories be
     * registered, so that they are in the class path of all class loaders
     * returned by future calls to
     * {@link #getClassLoader}.
     * @param dirs an array of directories with jar archives
     * @return true if there was at least one jar archive in <code>dirs</code>,
     * for wich the request was honored.
     **/
    public boolean registerLibraryDirectories(File[] dirs);
}
