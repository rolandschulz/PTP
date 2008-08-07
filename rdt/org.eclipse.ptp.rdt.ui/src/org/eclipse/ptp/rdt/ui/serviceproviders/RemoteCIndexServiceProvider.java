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

import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider;
import org.eclipse.ptp.internal.rdt.ui.contentassist.IContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.contentassist.RemoteContentAssistService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.ui.IMemento;

/**
 * @author crecoskie
 *
 */
public class RemoteCIndexServiceProvider extends AbstractRemoteCIndexServiceProvider implements IIndexServiceProvider2 {

	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteCIndexServiceProvider"; //$NON-NLS-1$
	private static final String HOST_NAME_KEY = "host-name"; //$NON-NLS-1$
	private RemoteSearchService fSearchService;
	private IContentAssistService fContentAssistService;
	
	private String fHostName;
	
	/**
	 * @param id
	 * @param name
	 * @param serviceId
	 */
	public RemoteCIndexServiceProvider(String id, String name, String serviceId) {
		super(id, name, serviceId);
	}

	/**
	 * 
	 */
	public RemoteCIndexServiceProvider() {
		this(ID, NAME, SERVICE_ID);
	}

	public void restoreState(IMemento providerMemento) {
		fHostName = providerMemento.getString(HOST_NAME_KEY);
	}

	public void saveState(IMemento providerMemento) {
		providerMemento.putString(HOST_NAME_KEY, fHost.getAliasName());
	}

	public ISearchService getSearchService() {
		if(!isConfigured())
			return null;
		
		if(fSearchService == null)
			fSearchService = new RemoteSearchService(fHost, fConnectorService);
		
		return fSearchService;
	}

	public IContentAssistService getContentAssistService() {
		if(!isConfigured())
			return null;
		
		if(fContentAssistService == null)
			fContentAssistService = new RemoteContentAssistService(fHost, fConnectorService);
		
		return fContentAssistService;
	}
	
	@Override
	public boolean isConfigured() {
		if (fHost == null && fHostName != null) {
			IHost[] hosts = SystemStartHere.getConnections();
			for (IHost host : hosts) {
				if (host.getAliasName().equals(fHostName)) {
					setConnection(host, getDStoreConnectorService(host));
				}
			}
			fHostName = null;
		}
		return super.isConfigured();
	}
	
	public static IConnectorService getDStoreConnectorService(IHost host) {
		IConnectorService[] connectorServices = host.getConnectorServices();
		
		for(int k = 0; k < connectorServices.length; k++) {
			if(connectorServices[k] instanceof DStoreConnectorService)
				return connectorServices[k];
		}
		
		return null;
	}
}
