package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

public interface IResourceVisitor
{
    public boolean visit(IResource resource) throws CoreException;
}
