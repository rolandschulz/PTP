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

public class ResourcesPlugin
{
    private static IWorkspace ws = new IWorkspace()
    {
        public IWorkspaceRoot getRoot()
        {
            return new WorkspaceRoot();
        }

        public void addResourceChangeListener(IResourceChangeListener listener, int eventMask)
        {
            Util.displayWarning("WARNING: IWorkspace#addResourceChangeListener not implemented");
        }
    };

    public static IWorkspace getWorkspace()
    {
        return ws;
    }
}
