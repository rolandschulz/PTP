package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

public interface IWorkspaceRoot extends IContainer
{
    IResource findMember(String path);

    void accept(IResourceVisitor resourceVisitor) throws CoreException;
}
