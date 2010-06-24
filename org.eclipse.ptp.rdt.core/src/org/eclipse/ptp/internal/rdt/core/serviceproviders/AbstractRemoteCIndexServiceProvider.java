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
package org.eclipse.ptp.internal.rdt.core.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.RemoteCallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.RemoteIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.model.IModelBuilderService;
import org.eclipse.ptp.internal.rdt.core.model.RemoteModelBuilderService;
import org.eclipse.ptp.internal.rdt.core.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.core.navigation.RemoteNavigationService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.RemoteTypeHierarchyService;
import org.eclipse.ptp.rdt.core.messages.Messages;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * Abstract class which forms the basis of a remote C/C++ indexing provider.
 * 
 * @author crecoskie
 * @noextend
 * @see org.eclipse.ptp.rdt.ui.serviceproviders.RemoteCIndexServiceProvider
 */
public abstract class AbstractRemoteCIndexServiceProvider extends ServiceProvider implements IIndexServiceProvider {

	protected boolean fIsConfigured;
	protected IHost fHost;
	protected IConnectorService fConnectorService;
	protected IIndexLifecycleService fIndexLifecycleService;
	protected INavigationService fNavigationService;
	protected ICallHierarchyService fCallHierarchyService;
	protected ITypeHierarchyService fTypeHierarchyService;
	protected IIncludeBrowserService fIncludeBrowserService;
	protected IModelBuilderService fModelBuilderService;
	protected String indexLocation;
	
	public static final String ID = "org.eclipse.ptp.rdt.core.RemoteCIndexServiceProvider"; //$NON-NLS-1$
	public static final String NAME = Messages.RemoteCIndexServiceProvider_0;
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
	
	public static final String HOST_NAME_KEY = "host-name"; //$NON-NLS-1$
	public static final String INDEX_LOCATION_KEY = "index-location"; //$NON-NLS-1$
	
	public synchronized ICallHierarchyService getCallHierarchyService() {
		if(!isConfigured())
			return null;
		
		if(fCallHierarchyService== null)
			fCallHierarchyService = new RemoteCallHierarchyService(fConnectorService);
		
		return fCallHierarchyService;
	}
	
	/**
	 * @return the system connection for this service provider
	 */
	public IHost getHost() {
		return fHost;
	}
	
	public synchronized IIncludeBrowserService getIncludeBrowserService() {
		if(!isConfigured())
			return null;
		
		if(fIncludeBrowserService== null)
			fIncludeBrowserService = new RemoteIncludeBrowserService(fConnectorService);
		
		return fIncludeBrowserService;
	}
	
	public synchronized IIndexLifecycleService getIndexLifeCycleService() {
		if(!isConfigured())
			return null;
		
		if(fIndexLifecycleService == null)
			fIndexLifecycleService = new RemoteIndexLifecycleService(fConnectorService);
		
		return fIndexLifecycleService;
	}
	
	public synchronized IModelBuilderService getModelBuilderService() {
		if(!isConfigured())
			return null;
		
		if(fModelBuilderService== null)
			fModelBuilderService = new RemoteModelBuilderService(fConnectorService);
		
		return fModelBuilderService;
	}
	
	public synchronized INavigationService getNavigationService() {
		if(!isConfigured())
			return null;
		
		if(fNavigationService== null)
			fNavigationService = new RemoteNavigationService(fConnectorService);
		
		return fNavigationService;
	}

	public synchronized ITypeHierarchyService getTypeHierarchyService() {
		if(!isConfigured())
			return null;
		
		if(fTypeHierarchyService== null)
			fTypeHierarchyService = new RemoteTypeHierarchyService(fConnectorService);
		
		return fTypeHierarchyService;
	}
		
	public boolean isConfigured() {
		return fIsConfigured;
	}
	
	public void setConfigured(boolean isConfigured) {
		fIsConfigured = isConfigured;
	}
	
	public void setConnection(IHost host, IConnectorService connectorService) {
		fHost = host;
		fConnectorService = connectorService;
		setHostName(host.getAliasName());
		setConfigured(true);
	}
	
 	/**
	 * Set the host name for this connection
	 * 
	 * @param hostName
	 */
	public void setHostName(String hostName) {
		putString(HOST_NAME_KEY, hostName);
 	}
	
	/**
	 * Get the host name for this connection.
	 * 
	 * @return host name
	 */
	public String getHostName() {
		return getString(HOST_NAME_KEY, null);
	}

	public void setIndexLocation(String path) {
		putString(INDEX_LOCATION_KEY, path);
	}
	
	public String getIndexLocation() {
		return getString(INDEX_LOCATION_KEY, null);
	}
	
	

}
