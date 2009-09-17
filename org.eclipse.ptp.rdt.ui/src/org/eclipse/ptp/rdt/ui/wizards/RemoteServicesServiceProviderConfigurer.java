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
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.IServiceProviderConfiguration;
import org.eclipse.swt.widgets.Shell;

/**
 * Allows the user to select a provider of Remote Services for a RemoteBuildServiceProvider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 * @see org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider
 * @deprecated
 */
public class RemoteServicesServiceProviderConfigurer implements IServiceProviderConfiguration {


	public void configureServiceProvider(IServiceProvider provider, Shell parentShell) {
		Dialog configDialog = new RemoteServicesProviderSelectionDialog(provider, parentShell);
		configDialog.open();
	}
	
}
