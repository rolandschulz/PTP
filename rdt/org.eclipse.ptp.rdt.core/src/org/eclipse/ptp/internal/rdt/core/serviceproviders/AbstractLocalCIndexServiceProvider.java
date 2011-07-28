/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.LocalCallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.LocalIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.model.IModelBuilderService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.LocalTypeHierarchyService;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 *
 */
public abstract class AbstractLocalCIndexServiceProvider extends ServiceProvider implements IIndexServiceProvider {

	protected IConnectorService fConnectorService;
	protected ICallHierarchyService fCallHierarchyService;
	protected ITypeHierarchyService fTypeHierarchyService;
	protected IIncludeBrowserService fIncludeBrowserService;

	
	public boolean isConfigured() {
		return true;
	}

	public void setIndexLocation(String path) {
		// do nothing
	}

	public String getIndexLocation() {
		return null;
	}
	
	/**
	 * Not needed by the local indexer.
	 */
	public synchronized IIndexLifecycleService getIndexLifeCycleService() {
		return null;
	}

	/**
	 * Not needed by the local indexer.
	 */
	public synchronized IModelBuilderService getModelBuilderService() {
		return null;
	}
	

	public synchronized ICallHierarchyService getCallHierarchyService() {
		if(fCallHierarchyService == null)
			fCallHierarchyService = new LocalCallHierarchyService();
		return fCallHierarchyService;
	}

	public synchronized ITypeHierarchyService getTypeHierarchyService() {
		if(fTypeHierarchyService == null)
			fTypeHierarchyService = new LocalTypeHierarchyService();
		return fTypeHierarchyService;
	}

	public synchronized IIncludeBrowserService getIncludeBrowserService() {
		if(fIncludeBrowserService == null)
			fIncludeBrowserService = new LocalIncludeBrowserService();
		return fIncludeBrowserService;
	}

	

	

}
