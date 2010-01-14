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
package org.eclipse.photran.internal.core;

import org.eclipse.core.resources.Util;
import org.eclipse.core.resources.WorkspaceRoot;
import org.eclipse.core.runtime.IPath;

public class Activator
{
    private static Activator activator;

    private Activator() {}

    public static Activator getDefault()
    {
        if (activator == null) activator = new Activator();
        return activator;
    }

    public IPath getStateLocation()
    {
        return Util.pathFor(WorkspaceRoot.WORKSPACE_STATE_DIR);
    }
}
