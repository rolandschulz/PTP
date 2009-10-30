/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class WorkspaceRoot implements IWorkspaceRoot
{
    public static final File WORKSPACE_ROOT_DIR = new File(".");
    public static final File WORKSPACE_STATE_DIR = new File(".db-");

    public void accept(IResourceVisitor resourceVisitor) throws CoreException
    {
        Util.traverse(WORKSPACE_ROOT_DIR, resourceVisitor);
    }

    public IResource findMember(String path)
    {
        File f = new File(path);
        if (f.exists() && f.isDirectory())
            return Util.folderFor(f);
        else
            return Util.fileFor(f);
    }

    public String getName()
    {
        return WORKSPACE_ROOT_DIR.getName();
    }

    public IPath getFullPath()
    {
        return Util.pathFor(WORKSPACE_ROOT_DIR);
    }

    public boolean isAccessible()
    {
        return true;
    }

	public IMarker createMarker(String type) throws CoreException
	{
		throw new UnsupportedOperationException();
	}
}
