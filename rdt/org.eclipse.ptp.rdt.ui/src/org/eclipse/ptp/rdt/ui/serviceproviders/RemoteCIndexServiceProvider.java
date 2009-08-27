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
 * An RSE-based provider of C/C++ indexing services.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class RemoteCIndexServiceProvider extends AbstractRemoteCIndexServiceProvider implements IIndexServiceProvider2 {

	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteCIndexServiceProvider"; //$NON-NLS-1$
	
	private static final String HOST_NAME_KEY = "host-name"; //$NON-NLS-1$
	private static final String INDEX_LOCATION_KEY = "index-location"; //$NON-NLS-1$
	
	private RemoteSearchService fSearchService;
	private IContentAssistService fContentAssistService;
	
	
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
		indexLocation = providerMemento.getString(INDEX_LOCATION_KEY);
	}

	public void saveState(IMemento providerMemento) {
		providerMemento.putString(HOST_NAME_KEY, fHostName);
		providerMemento.putString(INDEX_LOCATION_KEY, indexLocation);
	}

	public synchronized ISearchService getSearchService() {
		if(!isConfigured())
			return null;
		
		if(fSearchService == null)
			fSearchService = new RemoteSearchService(fHost, fConnectorService);
		
		return fSearchService;
	}

	public synchronized IContentAssistService getContentAssistService() {
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#getConfigurationString()
	 */
	public String getConfigurationString() {
		if (isConfigured()) {
			return fHost.getName();
		}			
		return null;
	}
	
	public String toString() {
		return "RemoteCIndexServiceProvider(" + fHostName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
