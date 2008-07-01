package org.eclipse.core.resources;

public interface IResource
{
    IPath getFullPath();

    boolean isAccessible();

    String getName();
}
