package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

public interface IContainer extends IResource
{
    void accept(IResourceVisitor visitor) throws CoreException;
}
