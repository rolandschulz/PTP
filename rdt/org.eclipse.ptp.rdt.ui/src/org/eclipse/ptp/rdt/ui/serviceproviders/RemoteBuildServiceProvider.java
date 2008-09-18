/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import java.text.MessageFormat;

import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceProviderDescriptor;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ui.IMemento;

/**
 * @author crecoskie
 * 
 * A build service provider that uses the Remote Tools API to provide execution services.
 *
 */
public class RemoteBuildServiceProvider extends ServiceProviderDescriptor implements IRemoteExecutionServiceProvider {
	
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID = "RemoteBuildServiceProvider.remoteToolsProviderID"; //$NON-NLS-1$
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME = "RemoteBuildServiceProvider.remoteToolsConnectionName"; //$NON-NLS-1$
	
	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteBuildServiceProvider"; //$NON-NLS-1$
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("RemoteBuildServiceProvider.0"); //$NON-NLS-1$

	private String fRemoteToolsProviderID;
	private IRemoteConnection fRemoteConnection;

	public RemoteBuildServiceProvider(String id, String name, String serviceId) {
		super(id, name, serviceId);
	}
	
	public RemoteBuildServiceProvider() {
		this(ID, NAME, SERVICE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		return (fRemoteToolsProviderID != null && fRemoteConnection != null);
	}
		
	/**
	 * Sets the ID of the Remote Tools provider that this provider should use for its execution services.
	 * 
	 * @param id
	 */
	public void setRemoteToolsProviderID(String id) {
		fRemoteToolsProviderID = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		/// restore the tools provider
		fRemoteToolsProviderID = memento.getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID);
		
		// restore the connection
		String connectionName = memento.getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME);
		fRemoteConnection = getRemoteServices().getConnectionManager().getConnection(connectionName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		/// store the tools provider ID
		memento.putString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, fRemoteToolsProviderID);
		
		// store the connection name
		memento.putString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, fRemoteConnection.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getRemoteServices()
	 */
	public IRemoteServices getRemoteServices() {
		return PTPRemoteCorePlugin.getDefault().getRemoteServices(fRemoteToolsProviderID);
	}

	/**
	 * Sets the connection that this provider should use for its execution services.
	 * 
	 * @param connection
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection) {
		fRemoteConnection = connection;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return fRemoteConnection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#getConfigurationString()
	 */
	public String getConfigurationString() {
		if (isConfigured()) {
			return getRemoteServices().getName() + ": " + fRemoteConnection.getName(); //$NON-NLS-1$
		}
		return null;
	}
	
	


}
