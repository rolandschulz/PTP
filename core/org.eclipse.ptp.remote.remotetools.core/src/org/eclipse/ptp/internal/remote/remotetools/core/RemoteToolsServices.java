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
package org.eclipse.ptp.internal.remote.remotetools.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.remote.core.AbstractRemoteServices;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServicesDescriptor;

public class RemoteToolsServices extends AbstractRemoteServices {
	public static final String REMOTE_TOOLS_ID = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$

	private static final String TARGET_ELEMENT_NAME = "Remote Host"; //$NON-NLS-1$

	/**
	 * Find the target type element for the PTP remote services target type.
	 * 
	 * @return PTP target type element or null if none can be found (shouldn't
	 *         happen)
	 */
	public static TargetTypeElement getTargetTypeElement() {
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		for (Object obj : targetMgr.getTypeElements()) {
			TargetTypeElement element = (TargetTypeElement) obj;
			if (element.getName().equals(TARGET_ELEMENT_NAME)) {
				return element;
			}
		}
		return null;
	}

	private final RemoteToolsConnectionManager connMgr = new RemoteToolsConnectionManager(this);

	public RemoteToolsServices(IRemoteServicesDescriptor descriptor) {
		super(descriptor);
	}

	public int getCapabilities() {
		return CAPABILITY_ADD_CONNECTIONS | CAPABILITY_EDIT_CONNECTIONS | CAPABILITY_REMOVE_CONNECTIONS
				| CAPABILITY_SUPPORTS_TCP_PORT_FORWARDING | CAPABILITY_SUPPORTS_X11_FORWARDING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getConnectionManager
	 * ()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean initialize(IProgressMonitor monitor) {
		return true;
	}
}
