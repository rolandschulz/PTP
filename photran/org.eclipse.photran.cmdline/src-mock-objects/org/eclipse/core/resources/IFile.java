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

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IFile extends IResource
{
    InputStream getContents() throws CoreException;

    InputStream getContents(boolean force) throws CoreException;

    String getName();

    long getLocalTimeStamp();

    IProject getProject();

    IResource getParent();

    IPath getLocation();

    String getFileExtension();

    boolean isReadOnly();
}
