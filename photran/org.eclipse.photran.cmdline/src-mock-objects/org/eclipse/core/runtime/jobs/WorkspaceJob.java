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
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

public abstract class WorkspaceJob
{
    public WorkspaceJob(String name)
    {
    }

    public void setRule(ISchedulingRule rule)
    {
    }

    public abstract IStatus runInWorkspace(IProgressMonitor monitor);

    public void schedule()
    {
        runInWorkspace(new NullProgressMonitor());
    }
}