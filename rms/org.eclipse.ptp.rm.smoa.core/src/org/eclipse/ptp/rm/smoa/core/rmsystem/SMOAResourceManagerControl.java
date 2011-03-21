/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration;
import org.eclipse.ptp.rm.smoa.core.rtsystem.SMOARuntimeSystem;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class SMOAResourceManagerControl extends AbstractRuntimeResourceManagerControl {
	/** Current configuration of this RM */
	/* package access */SMOAConfiguration configuration;

	public SMOAResourceManagerControl(SMOAResourceManagerConfiguration config) {
		super(config);
		configuration = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl#doControlJob
	 * (java.lang.String, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		if (operation.equals(TERMINATE_OPERATION)) {
			JobThread job = getResourceManager().getJobThread(jobId);
			if (job != null) {
				job.terminate();
			}
		}
		super.doControlJob(jobId, operation, monitor);
	}

	@Override
	protected SMOAResourceManager getResourceManager() {
		return (SMOAResourceManager) super.getResourceManager();
	}

	@Override
	protected SMOARuntimeSystem getRuntimeSystem() {
		final IRuntimeSystem rs = super.getRuntimeSystem();
		if (rs instanceof SMOARuntimeSystem) {
			return (SMOARuntimeSystem) rs;
		}
		throw new RuntimeException();
	}

}