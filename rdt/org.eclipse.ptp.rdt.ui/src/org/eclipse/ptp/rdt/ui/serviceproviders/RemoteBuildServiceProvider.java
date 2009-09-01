/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.services.core.ServiceProvider;

/**
 * A build service provider that uses the Remote Tools API to provide execution services.
 *  
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 */
public class RemoteBuildServiceProvider extends ServiceProvider implements IRemoteExecutionServiceProvider {
	
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID = "RemoteBuildServiceProvider.remoteToolsProviderID"; //$NON-NLS-1$
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME = "RemoteBuildServiceProvider.remoteToolsConnectionName"; //$NON-NLS-1$
	
	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteBuildServiceProvider"; //$NON-NLS-1$
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("RemoteBuildServiceProvider.0"); //$NON-NLS-1$

	private IRemoteConnection fRemoteConnection = null;


	public String getConfigurationString() {
		if (isConfigured()) {
			return getRemoteServices().getName() + ": " + getRemoteConnectionName(); //$NON-NLS-1$
		}
		return null;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getConnection()
	 */
	public IRemoteConnection getConnection() {
		if(fRemoteConnection == null && getRemoteConnectionName() != null) {
			IRemoteServices services = getRemoteServices();
			if (services != null) {
				IRemoteConnectionManager manager = services.getConnectionManager();
				if (manager != null) {
					fRemoteConnection = manager.getConnection(getRemoteConnectionName());
				}
			}
		}
		return fRemoteConnection;
	}
	
	/**
	 * Get the remote connection name
	 * 
	 * @return remote connection name or null if provider has not been configured
	 */
	public String getRemoteConnectionName() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getRemoteServices()
	 */
	public IRemoteServices getRemoteServices() {
		return PTPRemoteCorePlugin.getDefault().getRemoteServices(getRemoteToolsProviderID());
	}

	/**
	 * Gets the ID of the Remote Tools provider that this provider uses for its execution services.
	 * 
	 * @return remote tools provider ID
	 */
	public String getRemoteToolsProviderID() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, null);
	}

	
	public boolean isConfigured() {
		return (getRemoteToolsProviderID() != null && getRemoteConnectionName() != null);
	}
	
	/**
	 * Sets the connection that this provider should use for its execution services.
	 * 
	 * @param connection
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection) {
		fRemoteConnection = connection;
		putString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, connection.getName());
	}

	/**
	 * Sets the ID of the Remote Tools provider that this provider should use for its execution services.
	 * 
	 * @param id
	 */
	public void setRemoteToolsProviderID(String id) {
		putString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, id);
	}
	
	
	public String toString() {
		return "RemoteBuildServiceProvider(" + getRemoteConnectionName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
