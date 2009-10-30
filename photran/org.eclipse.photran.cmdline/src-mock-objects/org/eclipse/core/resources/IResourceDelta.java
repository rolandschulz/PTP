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

import org.eclipse.core.runtime.CoreException;

public interface IResourceDelta
{
    public static final int ADDED = 1;
    public static final int CHANGED = 2;
    public static final int REMOVED = 3;
    public static final int CONTENT = 0;
    public static final int REPLACED = 0;

    void accept(IResourceDeltaVisitor visitor) throws CoreException;
    IResource getResource();
    int getKind();
    int getFlags();
}
