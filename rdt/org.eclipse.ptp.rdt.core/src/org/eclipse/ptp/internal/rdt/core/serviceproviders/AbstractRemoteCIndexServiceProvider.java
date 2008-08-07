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
package org.eclipse.ptp.internal.rdt.core.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.RemoteCallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.index.INavigationService;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.RemoteTypeHierarchyService;
import org.eclipse.ptp.rdt.core.messages.Messages;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceProviderDescriptor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * Abstract class which forms the basis of a remote C/C++ indexing provider.
 * 
 * @author crecoskie
 * @noextend
 * @see org.eclipse.ptp.rdt.ui.serviceproviders.RemoteCIndexServiceProvider
 */
public abstract class AbstractRemoteCIndexServiceProvider extends ServiceProviderDescriptor implements IIndexServiceProvider {

	protected boolean fIsConfigured;
	protected IHost fHost;
	protected IConnectorService fConnectorService;
	protected IIndexLifecycleService fIndexLifecycleService;
	protected INavigationService fNavigationService;
	protected ICallHierarchyService fCallHierarchyService;
	protected ITypeHierarchyService fTypeHierarchyService;
	
	public static final String ID = "org.eclipse.ptp.rdt.core.RemoteCIndexServiceProvider"; //$NON-NLS-1$
	public static final String NAME = Messages.RemoteCIndexServiceProvider_0;
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
	
	public AbstractRemoteCIndexServiceProvider(String id, String name, String serviceId) {
		super(id, name, serviceId);
	}
	
	public void setConnection(IHost host, IConnectorService connectorService) {
		fHost = host;
		fConnectorService = connectorService;
		setConfigured(true);
	}
	
	public boolean isConfigured() {
		return fIsConfigured;
	}

	public void setConfigured(boolean isConfigured) {
		fIsConfigured = isConfigured;
	}
	
	public synchronized IIndexLifecycleService getIndexLifeCycleService() {
		if(!isConfigured())
			return null;
		
		if(fIndexLifecycleService == null)
			fIndexLifecycleService = new RemoteIndexLifecycleService(fHost, fConnectorService);
		
		return fIndexLifecycleService;
	}
	
	public INavigationService getNavigationService() {
		return null;
	}
	
	public ICallHierarchyService getCallHierarchyService() {
		if(!isConfigured())
			return null;
		
		if(fCallHierarchyService== null)
			fCallHierarchyService = new RemoteCallHierarchyService(fHost, fConnectorService);
		
		return fCallHierarchyService;
	}
	
	public ITypeHierarchyService getTypeHierarchyService() {
		if(!isConfigured())
			return null;
		
		if(fTypeHierarchyService== null)
			fTypeHierarchyService = new RemoteTypeHierarchyService(fHost, fConnectorService);
		
		return fTypeHierarchyService;
	}
	
	

}
