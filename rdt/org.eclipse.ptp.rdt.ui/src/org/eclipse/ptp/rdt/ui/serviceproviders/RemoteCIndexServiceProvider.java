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

import org.eclipse.ptp.internal.rdt.core.callhierarchy.RemoteCallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchService;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.ui.IMemento;

/**
 * @author crecoskie
 *
 */
public class RemoteCIndexServiceProvider extends AbstractRemoteCIndexServiceProvider implements IIndexServiceProvider2 {

	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteCIndexServiceProvider"; //$NON-NLS-1$
	private static final String HOST_NAME_KEY = "host-name"; //$NON-NLS-1$
	private RemoteSearchService fSearchService;
	
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
		IHost[] hosts = SystemStartHere.getConnections();
		String hostName = providerMemento.getString(HOST_NAME_KEY);
		for (IHost host : hosts) {
			if (host.getAliasName().equals(hostName)) {
				fHost = host;
			}
		}
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
}
