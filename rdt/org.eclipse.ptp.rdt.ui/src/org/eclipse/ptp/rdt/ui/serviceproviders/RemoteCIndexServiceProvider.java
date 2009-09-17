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

	
	private RemoteSearchService fSearchService;
	private IContentAssistService fContentAssistService;
	

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
	
	public IHost getHost() {
		initializeHost();
		return super.getHost();
	}
	
	public boolean isConfigured() {
		initializeHost();
		return super.isConfigured();
	}
	
	private void initializeHost() {
		if (fHost == null && getHostName() != null) {
			IHost[] hosts = SystemStartHere.getConnections();
			for (IHost host : hosts) {
				if (host.getAliasName().equals(getHostName())) {
					setConnection(host, getDStoreConnectorService(host));
				}
			}
		}
	}
	
	public static IConnectorService getDStoreConnectorService(IHost host) {
		IConnectorService[] connectorServices = host.getConnectorServices();
		
		for(int k = 0; k < connectorServices.length; k++) {
			if(connectorServices[k] instanceof DStoreConnectorService)
				return connectorServices[k];
		}
		
		return null;
	}
	

	public String getConfigurationString() {
		if (isConfigured()) {
			return fHost.getName();
		}			
		return null;
	}
	
	public String toString() {
		return "RemoteCIndexServiceProvider(" + getHostName() + "," + getIndexLocation() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
