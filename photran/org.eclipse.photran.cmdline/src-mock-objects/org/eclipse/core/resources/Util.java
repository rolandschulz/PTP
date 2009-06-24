package org.eclipse.core.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Util
{
    public static void traverse(final File file, IResourceVisitor resourceVisitor) throws CoreException
    {
        if (file.isDirectory())
        {
            for (String name : file.list())
                traverse(new File(file, name), resourceVisitor);
        }
        else
        {
            resourceVisitor.visit(Util.fileFor(file));
        }
    }

    public static IPath pathFor(final File file)
    {
        return new IPath()
        {
            public IPath addTrailingSeparator()
            {
                return this;
            }

            public String toOSString()
            {
                try
                {
                    return file.getCanonicalPath();
                }
                catch (IOException e)
                {
                    throw new Error(e);
                }
            }
            
            public String toString()
            {
                return toOSString();
            }
            
            @Override public int hashCode()
            {
                return toOSString().hashCode();
            }
            
            @Override public boolean equals(Object o)
            {
                return o instanceof IPath && toOSString().equals(((IPath)o).toOSString());
            }
        };
    }
    
    public static IFile fileFor(final File file)
    {
        return new IFile()
        {
            public IMarker createMarker(String text) throws CoreException
            {
                return null;
            }

            public InputStream getContents() throws CoreException
            {
                try
                {
                    return new BufferedInputStream(new FileInputStream(file));
                }
                catch (FileNotFoundException e)
                {
                    throw new CoreException(new Status(IStatus.ERROR,
                                                       "org.eclipse.photran.cmdline",
                                                       e.getMessage(),
                                                       e));
                }
            }

            public long getLocalTimeStamp()
            {
                return file.lastModified();
            }

            public String getName()
            {
                return file.getName();
            }

            public IResource getParent()
            {
                return Util.folderFor(file.getParentFile());
            }

            public IProject getProject()
            {
                return IProject.ROOT;
            }

            public IPath getFullPath()
            {
                return Util.pathFor(file);
            }

            public boolean isAccessible()
            {
                return file.exists() && file.canRead();
            }
            
            @Override public int hashCode()
            {
                return getFullPath().hashCode();
            }
            
            @Override public boolean equals(Object o)
            {
                return o instanceof IFile && getFullPath().equals(((IFile)o).getFullPath());
            }
        };
    }

    protected static IFolder folderFor(final File dir)
    {
        return new IFolder()
        {
            public IPath getFullPath()
            {
                return Util.pathFor(dir);
            }

            public boolean isAccessible()
            {
                return dir.exists() && dir.canRead();
            }

            public String getName()
            {
                return dir.getName();
            }

            @Override public int hashCode()
            {
                return getFullPath().hashCode();
            }
            
            @Override public boolean equals(Object o)
            {
                return o instanceof IFolder && getFullPath().equals(((IFolder)o).getFullPath());
            }

            public void accept(IResourceVisitor visitor) throws CoreException
            {
                traverse(dir, visitor);
            }

    		public IMarker createMarker(String type) throws CoreException
    		{
    			throw new UnsupportedOperationException();
    		}
        };
    }
}
