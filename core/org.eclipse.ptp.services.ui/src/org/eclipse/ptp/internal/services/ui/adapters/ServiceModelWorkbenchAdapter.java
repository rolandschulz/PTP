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

import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ui.model.WorkbenchAdapter;

public class ServiceModelWorkbenchAdapter extends WorkbenchAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		return ((IServiceModelManager)parentElement).getConfigurations().toArray();
	}
}
