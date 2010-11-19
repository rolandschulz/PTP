/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.remotemake;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ptp.rdt.core.activator.Activator;

/**
 * @author crecoskie
 *
 */
public class ResourceRefreshJob extends Job {

	private List<IResource> fResourcesToRefresh;
	
	public ResourceRefreshJob(List<IResource> resourcesToRefresh) {
		super(Messages.ResourceRefreshJob_0);
		fResourcesToRefresh = resourcesToRefresh;
		this.setRule(new MultiRule(fResourcesToRefresh.toArray(new ISchedulingRule[0])));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor arg0) {
		for(IResource resource : fResourcesToRefresh) {
			try {
				resource.refreshLocal(IResource.DEPTH_INFINITE, arg0);
			} catch (CoreException e) {
				return Activator.createStatus(Messages.ResourceRefreshJob_1, e);
			}
		}
		
		return Status.OK_STATUS;
	}

}
