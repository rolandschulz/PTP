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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.ServiceModelImages;
import org.eclipse.ui.model.WorkbenchAdapter;

public class ServiceProviderWorkbenchAdapter extends WorkbenchAdapter {
	private IServiceModelManager fManager = ServiceModelManager.getInstance();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return ServiceModelImages.getImageDescriptor(ServiceModelImages.IMG_SERVICE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		IServiceProvider provider = (IServiceProvider)object;
		IService service = fManager.getService(provider.getServiceId());
		return service.getName() + " Service (" + provider.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
