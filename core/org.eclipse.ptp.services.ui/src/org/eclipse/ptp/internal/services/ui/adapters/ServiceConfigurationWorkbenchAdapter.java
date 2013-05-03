/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.services.ui.adapters;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.model.WorkbenchAdapter;

public class ServiceConfigurationWorkbenchAdapter extends WorkbenchAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		IServiceConfiguration conf = getServiceConfiguration(parentElement);
		if (conf != null) {
			Set<IServiceProvider> providers = new HashSet<IServiceProvider>();
			for (IService service : conf.getServices()) {
				providers.add(conf.getServiceProvider(service));
			}
			return providers.toArray();
		}
		return null;
	}

	/**
	 * @param parentElement
	 * @return
	 */
	private IServiceConfiguration getServiceConfiguration(Object parentElement) {
		IServiceConfiguration conf = null;
		if (parentElement instanceof IAdaptable) {
			conf = (IServiceConfiguration) ((IAdaptable) parentElement).getAdapter(IServiceConfiguration.class);
		}
		return conf;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		final IServiceConfiguration conf = (IServiceConfiguration) object;
		return conf.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object object) {
		return ServiceModelManager.getInstance();
	}
}
