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
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.ui.IServiceProviderConfiguration;
import org.eclipse.swt.widgets.Shell;

/**
 * @author crecoskie
 *
 */
public class RemoteCIndexServiceProviderConfigurer implements IServiceProviderConfiguration {

	/**
	 * 
	 */
	public RemoteCIndexServiceProviderConfigurer() {
		// TODO Auto-generated constructor stub
	}

	public void configureServiceProvider(IServiceProvider provider,
			Shell parentShell) {
		Dialog configDialog =  new HostSelectionDialog(provider, parentShell);
		configDialog.open();
		
	}

}
