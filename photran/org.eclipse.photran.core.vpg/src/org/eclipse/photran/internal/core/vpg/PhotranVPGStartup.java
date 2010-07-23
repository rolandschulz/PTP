/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.vpg;

import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.photran.internal.core.Activator;
import org.eclipse.rephraserengine.core.vpg.eclipse.VPGSchedulingRule;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.internal.Workbench;

/**
 * Called by Eclipse when the VPG plug-in is loaded
 * (see the org.eclipse.ui.startup extension point).
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class PhotranVPGStartup implements IStartup
{
    public void earlyStartup()
	{
		// Load the VPG and the parser, and start the indexer thread
		PhotranVPG.getInstance().start();
		
		// Make sure the database is closed and flushed when the workbench shuts down
		Workbench.getInstance().addWorkbenchListener(new IWorkbenchListener()
		{
            public boolean preShutdown(IWorkbench workbench, boolean forced)
            {
                FlushDatabaseJob.scheduleNewInstance();
                return true;
            }

            public void postShutdown(IWorkbench workbench)
            {
            }
		});
	}

    private static final class FlushDatabaseJob extends WorkspaceJob
    {
        public static void scheduleNewInstance()
        {
            WorkspaceJob job = new FlushDatabaseJob();
            job.setRule(MultiRule.combine(VPGSchedulingRule.getInstance(),
                        ResourcesPlugin.getWorkspace().getRoot()));
            job.schedule();
        }
        
        private FlushDatabaseJob()
        {
            super(Messages.PhotranVPGStartup_FlushingDatabase);
        }

        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
        {
            try
            {
                PhotranVPG.getDatabase().flush();
            }
            catch (Throwable e)
            {
                Activator.log(e);
            }

            return Status.OK_STATUS;
        }
    }
}