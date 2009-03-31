/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.remote.core;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem;

public class AbstractRemoteProxyRuntimeSystem extends AbstractProxyRuntimeSystem {
	private AbstractRemoteProxyRuntimeClient proxy;
	
	public AbstractRemoteProxyRuntimeSystem(AbstractRemoteProxyRuntimeClient proxy, AttributeDefinitionManager manager) {
		super(proxy, manager);
		this.proxy = proxy;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem#shutdown()
	 */
	public void shutdown() throws CoreException {
		try {
			proxy.shutdown();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.WARNING, PTPCorePlugin.getUniqueIdentifier(), 
					IStatus.WARNING, e.getMessage(), null));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem#startup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {
		try {
			proxy.startup(monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.WARNING, PTPCorePlugin.getUniqueIdentifier(), 
					IStatus.WARNING, e.getMessage(), null));
		}
	}
}
