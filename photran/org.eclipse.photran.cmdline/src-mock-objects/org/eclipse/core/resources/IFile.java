package org.eclipse.core.resources;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

public interface IFile extends IResource
{
    InputStream getContents() throws CoreException;

    String getName();

    long getLocalTimeStamp();

    IProject getProject();

    IResource getParent();
}
