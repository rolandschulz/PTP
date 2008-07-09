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
package org.eclipse.ptp.rdt.core.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService;
import org.eclipse.ptp.internal.rdt.core.index.INavigationService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;

/**
 * @author crecoskie
 *
 */
public interface IIndexServiceProvider extends IServiceProvider {
	public IIndexLifecycleService getIndexLifeCycleService();
	
	public INavigationService getNavigationService();
	
	public ICallHierarchyService getCallHierarchyService();
	
	public ITypeHierarchyService getTypeHierarchyService();
}
