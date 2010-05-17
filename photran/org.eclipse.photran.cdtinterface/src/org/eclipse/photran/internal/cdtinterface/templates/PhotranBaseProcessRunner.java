/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.templates;

import java.util.Map;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Base class for a {@link ProcessRunner} that modifies a project's Managed Build configuration.
 * <p>
 * This class simply contains code that is common between {@link AddAllBinaryParsersProcessRunner}
 * and {@link AddFortranErrorParsersProcessRunner}.
 * 
 * @author Jeff Overbey
 */
public abstract class PhotranBaseProcessRunner extends ProcessRunner
{
    @Override
    public final void process(TemplateCore template,
                              ProcessArgument[] args,
                              String processId,
                              IProgressMonitor monitor) throws ProcessFailureException
    {
        try
        {
            Map<String, String> valueStore = template.getValueStore();

            IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            IProject proj = workspaceRoot.getProject(valueStore.get("projectName")); //$NON-NLS-1$
            if (!proj.exists()) return;

            IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
            if (info == null) return;

            IManagedProject mProj = info.getManagedProject();
            if (mProj == null) return;

            for (IConfiguration cf : mProj.getConfigurations())
                modify(proj, cf);

            ManagedBuildManager.saveBuildInfo(proj, true);
        }
        catch (Throwable e)
        {
            return;
        }
    }

    protected abstract void modify(IProject proj, IConfiguration configuration) throws CoreException;
}
