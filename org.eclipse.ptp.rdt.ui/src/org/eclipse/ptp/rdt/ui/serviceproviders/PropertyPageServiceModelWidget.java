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

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.ui.wizards.ServiceModelWidget;

public class PropertyPageServiceModelWidget extends ServiceModelWidget {
	
	/**
	 * Find available remote services and service providers for the given project and
	 * add them to the table
	 * @param project
	 */
	public void updateServicesTable(IProject project) {
		fTable.removeAll();
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		getContributedServices(project);		
	}
	

}
