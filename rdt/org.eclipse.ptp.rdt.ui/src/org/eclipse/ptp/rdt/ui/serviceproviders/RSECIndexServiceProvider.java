/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteCCodeFoldingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteCodeFormattingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteInactiveHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteSemanticHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteCCodeFoldingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteCodeFormattingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteInactiveHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteSemanticHighlightingService;
import org.eclipse.ptp.internal.rdt.ui.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.ui.navigation.RemoteNavigationService;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchService;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * An RSE-based provider of C/C++ indexing services.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 * @since 2.0
 * 
 */
public class RSECIndexServiceProvider extends AbstractRemoteCIndexServiceProvider implements IIndexServiceProvider3 {

	public static final String ID = "org.eclipse.ptp.rdt.ui.RSECIndexServiceProvider"; //$NON-NLS-1$

	private RemoteSearchService fSearchService;
	private IContentAssistService fContentAssistService;
	private INavigationService fNavigationService;
	private IRemoteSemanticHighlightingService fRemoteSemanticHighlightingService;
	private IRemoteInactiveHighlightingService fRemoteInactiveHighlightingService;
	private IRemoteCCodeFoldingService fRemoteCCodeFoldingService;
	private IRemoteCodeFormattingService fRemoteCodeFormattingService;

	/**
	 * @since 4.1
	 */
	public boolean isRemote() {
		return true;
	}
	
	public synchronized ISearchService getSearchService() {
		if (!isConfigured())
			return null;

		if (fSearchService == null)
			fSearchService = new RemoteSearchService(fConnectorService);

		return fSearchService;
	}

	public synchronized IContentAssistService getContentAssistService() {
		if (!isConfigured())
			return null;

		if (fContentAssistService == null)
			fContentAssistService = new RemoteContentAssistService(fConnectorService);

		return fContentAssistService;
	}

	/**
	 * @since 4.1
	 */
	public synchronized INavigationService getNavigationService() {
		if(!isConfigured())
			return null;
		
		if(fNavigationService== null)
			fNavigationService = new RemoteNavigationService(fConnectorService);
		
		return fNavigationService;
	}
	
	@Override
	public IHost getHost() {
		initializeHost();
		return super.getHost();
	}

	@Override
	public boolean isConfigured() {
		initializeHost();
		return super.isConfigured();
	}

	private void initializeHost() {
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			UIPlugin.log(e);
			return;
		}
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

		for (int k = 0; k < connectorServices.length; k++) {
			if (connectorServices[k] instanceof DStoreConnectorService)
				return connectorServices[k];
		}

		return null;
	}

	@Override
	public String getConfigurationString() {
		if (isConfigured()) {
			return fHost.getName();
		}
		return null;
	}

	@Override
	public String toString() {
		return "RSECIndexServiceProvider(" + getHostName() + "," + getIndexLocation() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @since 4.1
	 */
	public synchronized IRemoteSemanticHighlightingService getRemoteSemanticHighlightingService() {
		if(!isConfigured())
			return null;

		if(fRemoteSemanticHighlightingService== null)
			fRemoteSemanticHighlightingService = new RemoteSemanticHighlightingService(fConnectorService);

		return fRemoteSemanticHighlightingService;
	}

	/**
	 * @since 4.3
	 */
	public IRemoteInactiveHighlightingService getRemoteInactiveHighlightingService() {
		if(!isConfigured())
			return null;

		if(fRemoteInactiveHighlightingService== null)
			fRemoteInactiveHighlightingService = new RemoteInactiveHighlightingService(fConnectorService);

		return fRemoteInactiveHighlightingService;
	}

	/**
	 * @since 4.1
	 */
	public IRemoteCCodeFoldingService getRemoteCodeFoldingService() {
		if(!isConfigured())
			return null;

		if(fRemoteCCodeFoldingService== null)
			fRemoteCCodeFoldingService = new RemoteCCodeFoldingService(fConnectorService);

		return fRemoteCCodeFoldingService;
	}

	/**
	 * @since 4.3
	 */
	public IRemoteCodeFormattingService getRemoteCodeFormattingService() {
		if(!isConfigured())
			return null;

		if(fRemoteCodeFormattingService== null)
			fRemoteCodeFormattingService = new RemoteCodeFormattingService(fConnectorService);

		return fRemoteCodeFormattingService;
	}
}
