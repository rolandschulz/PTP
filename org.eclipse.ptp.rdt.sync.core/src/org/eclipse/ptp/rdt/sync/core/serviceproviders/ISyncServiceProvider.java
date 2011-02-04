/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.serviceproviders;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Provides synchronization services.
 */
public interface ISyncServiceProvider extends IServiceProvider {

	/**
	 * Performs synchronization.
	 * 
	 * @param delta
	 *            resources requiring synchronization
	 * @param monitor
	 *            progress monitor for monitoring or canceling synch
	 * @throws CoreException
	 *             if synchronization fails
	 */
	public void ensureSync(IResourceDelta delta, IProgressMonitor monitor) throws CoreException;
}
