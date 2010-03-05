/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Represents either an {@link IFile} or a {@link java.io.File}.
 * <p>
 * Since {@link IFile} and {@link java.io.File} share no common superclass other than object, this
 * &quot;wrapper class&quot; can be used instead.  It provides a path-based equality comparison,
 * a {@link #toString()} methods that displays a user-readable file path, and an easy way to
 * retrieve both an {@link IFile} and a {@link java.io.File} for the same file (if possible).
 * <p>
 * This class was added mainly to support the C preprocessor.  A Fortran program containing C
 * preprocessor directives may <i>#include</i> any file in the local filesystem.  When the included
 * file is in the user's workspace, it should be represented as an {@link IFile}, since
 * {@link IFile}s can be displayed in search results, etc.; if the file cannot be represented as
 * an {@link IFile}, then a {@link java.io.File} must suffice.
 * 
 * @author Jeff Overbey
 */
public class FileOrIFile
{
    protected IFile ifile;
    protected java.io.File javaFile;

    public FileOrIFile(IFile file)
    {
        this.ifile = file;
        
        IPath location = file == null ? null : file.getLocation();
        this.javaFile = location == null ? null : location.toFile();
    }
    
    public FileOrIFile(java.io.File file)
    {
        this.javaFile = file;
        this.ifile = getIFileFor(file);
    }

    private static IFile getIFileFor(java.io.File file)
    {
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI());
        return files.length == 0 ? null : files[0];
    }
    
    /**
     * @return the <code>IFile</code> in which this token occurs, or
     * <code>null</code> if it is not located in a workspace file
     * 
     * @see #getFilenameToDisplayToUser()
     * @see #getJavaFile()
     */
    public IFile getIFile()
    {
        return ifile;
    }

    /**
     * @return the <code>java.io.File</code> in which this token occurs, or
     * <code>null</code> if it is not located on the local filesystem
     * 
     * @see #getFilenameToDisplayToUser()
     * @see #getJavaFile()
     */
    public java.io.File getJavaFile()
    {
        return javaFile;
    }

    /**
     * @return a filename that can be displayed to the user to indicate what
     * file this token is located in.  This is not guaranteed to be a legal
     * file path or even an actual filename.
     * 
     * @see #getIFile()
     * @see #getJavaFile()
     */
    public String toString()
    {
        if (this.ifile != null)
            return this.ifile.getFullPath().toOSString();
        else if (this.javaFile != null)
            return this.javaFile.getAbsolutePath();
        else
            return "?";
    }

    public boolean equals(Object o)
    {
        if (o == null || !o.getClass().equals(this.getClass())) return false;
        
        FileOrIFile that = (FileOrIFile)o;
        return equals(this.ifile, that.ifile)
            && equals(this.javaFile, that.javaFile);
    }

    private boolean equals(Object a, Object b)
    {
        if (a == b)
            return true;
        else if ((a == null) != (b == null))
            return false;
        else
            return a.equals(b);
    }
    
    public int hashCode()
    {
        return 13*hashCode(this.ifile) + hashCode(this.javaFile);
    }

    private int hashCode(Object o)
    {
        return o == null ? 0 : o.hashCode();
    }
}
