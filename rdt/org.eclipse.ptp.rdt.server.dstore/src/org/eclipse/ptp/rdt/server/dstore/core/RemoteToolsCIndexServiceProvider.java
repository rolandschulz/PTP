/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.server.dstore.core;

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
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.RemoteTypeHierarchyService;
import org.eclipse.ptp.internal.rdt.ui.contentassist.IContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.contentassist.RemoteContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteCCodeFoldingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteCodeFormattingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteSemanticHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteCCodeFoldingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteCodeFormattingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteSemanticHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.ui.navigation.RemoteNavigationService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchService;
import org.eclipse.ptp.rdt.server.dstore.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.IRemoteToolsIndexServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.core.ServiceProvider;

/**
 * @since 2.0
 */
public class RemoteToolsCIndexServiceProvider extends ServiceProvider implements IRemoteToolsIndexServiceProvider,
		IServiceProviderWorkingCopy {

	protected IIndexLifecycleService fIndexLifecycleService = null;
	protected INavigationService fNavigationService = null;
	protected ICallHierarchyService fCallHierarchyService = null;
	protected ITypeHierarchyService fTypeHierarchyService = null;
	protected IIncludeBrowserService fIncludeBrowserService = null;
	protected IModelBuilderService fModelBuilderService = null;
	protected RemoteSearchService fSearchService = null;
	protected IContentAssistService fContentAssistService = null;
	/**
	 * @since 3.0
	 */
	protected RemoteSemanticHighlightingService fRemoteSemanticHighlightingService = null;
	/**
	 * @since 3.0
	 */
	protected RemoteCCodeFoldingService fRemoteCCodeFoldingService = null;
	/**
	 * @since 3.2
	 */
	protected IRemoteCodeFormattingService fRemoteCodeFormattingService = null;
	protected RemoteToolsCIndexSubsystem fSubsystem = null;
	protected boolean fIsDirty = false;
	protected RemoteToolsCIndexServiceProvider fProvider = null;
	protected boolean fIsConfigured = false;

	public static final String NAME = Messages.RemoteToolsCIndexServiceProvider_0;
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	public static final String SERVICE_ID_KEY = "service-name"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String CONNECTION_NAME_KEY = "connection-name"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String INDEX_LOCATION_KEY = "index-location"; //$NON-NLS-1$

	public RemoteToolsCIndexServiceProvider() {
	}

	public RemoteToolsCIndexServiceProvider(RemoteToolsCIndexServiceProvider provider) {
		fProvider = provider;
		setProperties(provider.getProperties());
		setDescriptor(provider.getDescriptor());
	}

	/**
	 * @since 3.0
	 */
	public boolean isRemote() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#copy()
	 */
	@Override
	public IServiceProviderWorkingCopy copy() {
		return new RemoteToolsCIndexServiceProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getCallHierarchyService()
	 */
	public synchronized ICallHierarchyService getCallHierarchyService() {
		if (!isConfigured()) {
			return null;
		}

		if (fCallHierarchyService == null) {
			fCallHierarchyService = new RemoteCallHierarchyService(fSubsystem);
		}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2#
	 * getContentAssistService()
	 */
	public IContentAssistService getContentAssistService() {
		if (!isConfigured()) {
			return null;
		}

		if (fContentAssistService == null) {
			fContentAssistService = new RemoteContentAssistService(fSubsystem);
		}

		return fContentAssistService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getIncludeBrowserService()
	 */
	public synchronized IIncludeBrowserService getIncludeBrowserService() {
		if (!isConfigured()) {
			return null;
		}

		if (fIncludeBrowserService == null) {
			fIncludeBrowserService = new RemoteIncludeBrowserService(fSubsystem);
		}

		return fIncludeBrowserService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getIndexLifeCycleService()
	 */
	public synchronized IIndexLifecycleService getIndexLifeCycleService() {
		if (!isConfigured()) {
			return null;
		}

		if (fIndexLifecycleService == null) {
			fIndexLifecycleService = new RemoteIndexLifecycleService(fSubsystem);
		}

		return fIndexLifecycleService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getIndexLocation()
	 */
	public String getIndexLocation() {
		initialize();
		return getString(INDEX_LOCATION_KEY, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getModelBuilderService()
	 */
	public synchronized IModelBuilderService getModelBuilderService() {
		if (!isConfigured()) {
			return null;
		}

		if (fModelBuilderService == null) {
			fModelBuilderService = new RemoteModelBuilderService(fSubsystem);
		}

		return fModelBuilderService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getNavigationService()
	 */
	/**
	 * @since 3.0
	 */
	public synchronized INavigationService getNavigationService() {
		if (!isConfigured()) {
			return null;
		}

		if (fNavigationService == null) {
			fNavigationService = new RemoteNavigationService(fSubsystem);
		}

		return fNavigationService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#getOriginal()
	 */
	public IServiceProvider getOriginal() {
		return fProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.ui.serviceproviders.IRemoteToolsIndexServiceProvider#
	 * getConnection()
	 */
	public IRemoteConnection getConnection() {
		if (!isConfigured()) {
			return null;
		}
		return getRemoteServices().getConnectionManager().getConnection(getConnectionName());
	}

	private IRemoteServices getRemoteServices() {
		if (!isConfigured()) {
			return null;
		}
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(getServiceId());
		if (!services.isInitialized()) {
			services.initialize();
		}
		if (!services.isInitialized()) {
			return null;
		}
		return services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2#
	 * getSearchService()
	 */
	public ISearchService getSearchService() {
		if (!isConfigured()) {
			return null;
		}

		if (fSearchService == null) {
			fSearchService = new RemoteSearchService(fSubsystem);
		}

		return fSearchService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#getServiceId()
	 */
	@Override
	public String getServiceId() {
		return getString(SERVICE_ID_KEY, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * getTypeHierarchyService()
	 */
	public synchronized ITypeHierarchyService getTypeHierarchyService() {
		if (!isConfigured()) {
			return null;
		}

		if (fTypeHierarchyService == null) {
			fTypeHierarchyService = new RemoteTypeHierarchyService(fSubsystem);
		}

		return fTypeHierarchyService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		initialize();
		return fIsConfigured;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#isDirty()
	 */
	public boolean isDirty() {
		return fIsDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#putString(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void putString(String key, String value) {
		fIsDirty = true;
		super.putString(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#save()
	 */
	public void save() {
		if (fProvider != null) {
			fProvider.setProperties(getProperties());
			fIsDirty = false;
		}
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
	 * @param connection
	 *            new connection
	 */
	public void setConnection(IRemoteConnection connection) {
		setConnection(connection, true);
	}

	/**
	 * Set the host name for this connection
	 * 
	 * @param hostName
	 */
	public void setConnectionName(String connectionName) {
		putString(CONNECTION_NAME_KEY, connectionName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider#
	 * setIndexLocation(java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemoteToolsCIndexServiceProvider(" + getIndexLocation() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void initialize() {
		if (fSubsystem == null && getServiceId() != null) {
			IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(getServiceId());
			if (services != null) {
				if (!services.isInitialized()) {
					services.initialize();
				}
				if (services.isInitialized() && getConnectionName() != null) {
					IRemoteConnection connection = services.getConnectionManager().getConnection(getConnectionName());
					setConnection(connection, false);
				}
			}
		}
	}

	/**
	 * Set a new connection for this service provider. If reset is true the
	 * index and DStore server locations will be reset to their default values.
	 * 
	 * @param connection
	 *            new connection
	 * @param reset
	 *            reset locations to defaults
	 */
	private void setConnection(IRemoteConnection connection, boolean reset) {
		setServiceId(connection.getRemoteServices().getId());
		setConnectionName(connection.getName());
		if (reset) {
			IPath workingDir = new Path(connection.getWorkingDirectory());
			setIndexLocation(workingDir.append(".eclipsesettings").toString()); //$NON-NLS-1$
		}
		if (fSubsystem != null) {
			fSubsystem.dispose();
		}
		fSubsystem = new RemoteToolsCIndexSubsystem(this);
		setConfigured(true);
	}

	/**
	 * @since 3.0
	 */
	public IRemoteSemanticHighlightingService getRemoteSemanticHighlightingService() {
		if (!isConfigured()) {
			return null;
		}

		if (fRemoteSemanticHighlightingService == null) {
			fRemoteSemanticHighlightingService = new RemoteSemanticHighlightingService(fSubsystem);
		}

		return fRemoteSemanticHighlightingService;
	}

	/**
	 * @since 3.0
	 */
	public IRemoteCCodeFoldingService getRemoteCodeFoldingService() {
		if (!isConfigured()) {
			return null;
		}

		if (fRemoteCCodeFoldingService == null) {
			fRemoteCCodeFoldingService = new RemoteCCodeFoldingService(fSubsystem);
		}

		return fRemoteCCodeFoldingService;
	}

	/**
	 * @since 3.2
	 */
	public IRemoteCodeFormattingService getRemoteCodeFormattingService() {
		if (!isConfigured()) {
			return null;
		}

		if (fRemoteCodeFormattingService == null) {
			fRemoteCodeFormattingService = new RemoteCodeFormattingService(fSubsystem);
		}

		return fRemoteCodeFormattingService;
	}
}
