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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.ptp.internal.rdt.ui.contentassist.IContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.contentassist.RemoteContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchService;
import org.eclipse.ptp.rdt.core.messages.Messages;
import org.eclipse.ptp.rdt.ui.subsystems.RemoteCIndexSubsystem2;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.services.core.ServiceProvider;

public class RemoteCIndexServiceProvider2 extends ServiceProvider implements IIndexServiceProvider2 {

	protected boolean fIsConfigured;

	protected IIndexLifecycleService fIndexLifecycleService;
	protected INavigationService fNavigationService;
	protected ICallHierarchyService fCallHierarchyService;
	protected ITypeHierarchyService fTypeHierarchyService;
	protected IIncludeBrowserService fIncludeBrowserService;
	protected IModelBuilderService fModelBuilderService;
	protected RemoteSearchService fSearchService;
	protected IContentAssistService fContentAssistService;
	protected RemoteCIndexSubsystem2 fSubsystem = null;
	protected String indexLocation;
	
	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteCIndexServiceProvider2"; //$NON-NLS-1$
	public static final String NAME = Messages.RemoteCIndexServiceProvider_0;
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
	
	private static final String SERVICE_ID_KEY = "service-name"; //$NON-NLS-1$
	private static final String CONNECTION_NAME_KEY = "connection-name"; //$NON-NLS-1$
	private static final String INDEX_LOCATION_KEY = "index-location"; //$NON-NLS-1$
	private static final String DSTORE_LOCATION_KEY = "dstore-location"; //$NON-NLS-1$
	private static final String DSTORE_COMMAND_KEY = "dstore-command"; //$NON-NLS-1$
	private static final String DSTORE_ENV_KEY = "dstore-env"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getCallHierarchyService()
	 */
	public synchronized ICallHierarchyService getCallHierarchyService() {
		if(!isConfigured())
			return null;
		
		if(fCallHierarchyService== null)
			fCallHierarchyService = new RemoteCallHierarchyService(fSubsystem);
		
		return fCallHierarchyService;
	}
	
	/**
	 * Get the host name for this connection.
	 * 
	 * @return host name
	 */
	public String getConnectionName() {
		return getString(CONNECTION_NAME_KEY, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2#getContentAssistService()
	 */
	public IContentAssistService getContentAssistService() {
		if(!isConfigured())
			return null;
		
		if(fContentAssistService == null)
			fContentAssistService = new RemoteContentAssistService(fSubsystem);
		
		return fContentAssistService;
	}
	
	public String getDStoreEnv() {
		initialize();
		return getString(DSTORE_ENV_KEY, ""); //$NON-NLS-1$
	}
	
	public String getDStoreCommand() {
		initialize();
		return getString(DSTORE_COMMAND_KEY, ""); //$NON-NLS-1$
	}
	
	public String getDStoreLocation() {
		initialize();
		return getString(DSTORE_LOCATION_KEY, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getIncludeBrowserService()
	 */
	public synchronized IIncludeBrowserService getIncludeBrowserService() {
		if(!isConfigured())
			return null;
		
		if(fIncludeBrowserService== null)
			fIncludeBrowserService = new RemoteIncludeBrowserService(fSubsystem);
		
		return fIncludeBrowserService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getIndexLifeCycleService()
	 */
	public synchronized IIndexLifecycleService getIndexLifeCycleService() {
		if(!isConfigured())
			return null;
		
		if(fIndexLifecycleService == null)
			fIndexLifecycleService = new RemoteIndexLifecycleService(fSubsystem);
		
		return fIndexLifecycleService;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getIndexLocation()
	 */
	public String getIndexLocation() {
		initialize();
		return getString(INDEX_LOCATION_KEY, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getModelBuilderService()
	 */
	public synchronized IModelBuilderService getModelBuilderService() {
		if(!isConfigured())
			return null;
		
		if(fModelBuilderService== null)
			fModelBuilderService = new RemoteModelBuilderService(fSubsystem);
		
		return fModelBuilderService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getNavigationService()
	 */
	public synchronized INavigationService getNavigationService() {
		if(!isConfigured())
			return null;
		
		if(fNavigationService== null)
			fNavigationService = new RemoteNavigationService(fSubsystem);
		
		return fNavigationService;
	}
	
	public IRemoteConnection getRemoteConnection() {
		if (!isConfigured()) {
			return null;
		}
		return getRemoteServices().getConnectionManager().getConnection(getConnectionName());
	}
	
	public IRemoteServices getRemoteServices() {
		if (!isConfigured()) {
			return null;
		}
		return PTPRemoteCorePlugin.getDefault().getRemoteServices(getServiceId());
	}
	
 	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2#getSearchService()
	 */
	public ISearchService getSearchService() {
		if(!isConfigured())
			return null;
		
		if(fSearchService == null)
			fSearchService = new RemoteSearchService(fSubsystem);
		
		return fSearchService;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.ServiceProvider#getServiceId()
	 */
	@Override
	public String getServiceId() {
		return getString(SERVICE_ID_KEY, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#getTypeHierarchyService()
	 */
	public synchronized ITypeHierarchyService getTypeHierarchyService() {
		if(!isConfigured())
			return null;
		
		if(fTypeHierarchyService== null)
			fTypeHierarchyService = new RemoteTypeHierarchyService(fSubsystem);
		
		return fTypeHierarchyService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		initialize();
		return fIsConfigured;
	}
	
	/**
 	 * @param isConfigured
 	 */
 	public void setConfigured(boolean isConfigured) {
		fIsConfigured = isConfigured;
	}
	
	/**
	 * Set a new connection for this service provider. This will reset the index
	 * and DStore server locations to their default values.
	 * 
	 * @param services new remote service provider
	 * @param connection new connection
	 */
	public void setConnection(IRemoteServices services, IRemoteConnection connection) {
		setConnection(services, connection, true);
	}
	
	/**
	 * Set the host name for this connection
	 * 
	 * @param hostName
	 */
	public void setConnectionName(String connectionName) {
		putString(CONNECTION_NAME_KEY, connectionName);
 	}
	
	public void setDStoreEnv(String env) {
		putString(DSTORE_ENV_KEY, env);
	}
	
	public void setDStoreCommand(String command) {
		putString(DSTORE_COMMAND_KEY, command);
	}
	
	public void setDStoreLocation(String path) {
		putString(DSTORE_LOCATION_KEY, path);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#setIndexLocation(java.lang.String)
	 */
	public void setIndexLocation(String path) {
		putString(INDEX_LOCATION_KEY, path);
	}
	
	/**
	 * @param serviceId
	 */
	public void setServiceId(String serviceId) {
		putString(SERVICE_ID_KEY, serviceId);
 	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemoteCIndexServiceProvider2(" + getIndexLocation() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void initialize() {
		if (fSubsystem == null && getServiceId() != null) {
			IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(getServiceId());
			if (services != null && getConnectionName() != null) {
				IRemoteConnection connection = services.getConnectionManager().getConnection(getConnectionName());
				setConnection(services, connection, false);
			}
		}
	}
	
	/**
	 * Set a new connection for this service provider. If reset is true the index
	 * and DStore server locations will be reset to their default values.
	 * 
	 * @param services new remote service provider
	 * @param connection new connection
	 * @param reset reset locations to defaults
	 */
	private void setConnection(IRemoteServices services, IRemoteConnection connection, boolean reset) {
		setServiceId(services.getId());
		setConnectionName(connection.getName());
		if (reset) {
			IPath workingDir = new Path(services.getFileManager(connection).getWorkingDirectory());
			setIndexLocation(workingDir.append(".eclipsesettings").toString()); //$NON-NLS-1$
			setDStoreLocation(workingDir.toString());
		}
		if (fSubsystem != null) {
			fSubsystem.dispose();
		}
		fSubsystem = new RemoteCIndexSubsystem2(this);
		setConfigured(true);
	}

}
