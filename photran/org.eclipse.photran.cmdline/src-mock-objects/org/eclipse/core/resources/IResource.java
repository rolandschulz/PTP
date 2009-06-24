package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IResource
{
    IPath getFullPath();

    boolean isAccessible();

    String getName();

	IMarker createMarker(String type) throws CoreException;
}
