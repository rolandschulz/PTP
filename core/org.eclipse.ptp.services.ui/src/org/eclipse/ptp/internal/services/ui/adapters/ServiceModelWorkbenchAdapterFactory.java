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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

public class ServiceModelWorkbenchAdapterFactory implements IAdapterFactory {

	private static final ServiceModelWorkbenchAdapter SERVICE_MODEL_WORKBENCH_ADAPTER = new ServiceModelWorkbenchAdapter();
	private static final ServiceConfigurationWorkbenchAdapter SERVICE_CONFIGURATION_WORKBENCH_ADAPTER = new ServiceConfigurationWorkbenchAdapter();
	private static final ServiceProviderWorkbenchAdapter SERVICE_PROVIDER_WORKBENCH_ADAPTER = new ServiceProviderWorkbenchAdapter();
	private static final ServiceWorkbenchAdapter SERVICE_WORKBENCH_ADAPTER = new ServiceWorkbenchAdapter();

	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class || adapterType == IWorkbenchAdapter2.class) {
			if (adaptableObject instanceof IServiceModelManager) {
				return SERVICE_MODEL_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IServiceConfiguration) {
				return SERVICE_CONFIGURATION_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IServiceProvider) {
				return SERVICE_PROVIDER_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IService) {
				return SERVICE_WORKBENCH_ADAPTER;
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class };
	}

}
