/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.core;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService;

/**
 * Simple class with a single public method for sync'ing. This serves as an adapter for an ISynchronizeService so that clients
 * can use the provider but without the ability to change the provider or read from it. As of 6.0.0, sync providers are dynamic
 * and may handle multiple projects and build configurations. Thus, clients need a way to use the provider but without creating
 * a dependency on the provider's internal data.
 */
public class SyncRunner {
	private ISynchronizeService provider;
	
	public SyncRunner(ISynchronizeService ssp) {
		provider = ssp;
	}
	
	public void synchronize(IProject project, SyncConfig syncConfig, IResourceDelta delta, SyncFileFilter filter,
			IProgressMonitor monitor, EnumSet<SyncFlag> syncFlags) throws CoreException {
		provider.synchronize(project, syncConfig, delta, filter, monitor, syncFlags);
	}
}