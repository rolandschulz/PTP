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
package org.eclipse.ptp.rdt.core.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.model.IModelBuilderService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Interface for providers of C Indexing based services.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public interface IIndexServiceProvider extends IServiceProvider {
	
	/**
	 * @since 4.0
	 */
	public boolean isRemote();
	
	public IIndexLifecycleService getIndexLifeCycleService();
	
	public ICallHierarchyService getCallHierarchyService();
	
	public ITypeHierarchyService getTypeHierarchyService();
	
	public IIncludeBrowserService getIncludeBrowserService();
	
	public IModelBuilderService getModelBuilderService();
	
	
	public void setIndexLocation(String path);
	
	public String getIndexLocation();
}
