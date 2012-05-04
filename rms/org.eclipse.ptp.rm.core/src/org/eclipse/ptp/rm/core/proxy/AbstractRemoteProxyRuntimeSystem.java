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

package org.eclipse.ptp.rm.core.proxy;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem;

/**
 * @since 4.0
 */
public abstract class AbstractRemoteProxyRuntimeSystem extends AbstractProxyRuntimeSystem {
	private final AbstractRemoteProxyRuntimeClient fRemoteProxy;

	/**
	 * @since 3.0
	 */
	public AbstractRemoteProxyRuntimeSystem(AbstractRemoteProxyRuntimeClient proxy) {
		super(proxy);
		fRemoteProxy = proxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem#shutdown()
	 */
	@Override
	public void shutdown() throws CoreException {
		try {
			fRemoteProxy.shutdown();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.WARNING, PTPCorePlugin.getUniqueIdentifier(), IStatus.WARNING,
					e.getMessage(), null));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem#startup(org.eclipse .core.runtime.IProgressMonitor)
	 */
	@Override
	public void startup(IProgressMonitor monitor) throws CoreException {
		initialize();
		try {
			fRemoteProxy.startup(monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.WARNING, PTPCorePlugin.getUniqueIdentifier(), IStatus.WARNING,
					e.getMessage(), null));
		}
	}
}
