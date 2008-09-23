/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.services.ui;

import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface that supplies a UI which can be launched to configure a service provider.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @see IServiceProvider
 */
public interface IServiceProviderConfiguration {
	/**
	 * Configure the service provider, which typically launches a dialog/wizard to allow the 
	 * service provider to be configured (may include setting up and associating any required connections)
	 * 
	 * @param provider
	 * @param parentShell parent SWT shell of the UI that is to be launched
	 */
	public void configureServiceProvider(IServiceProvider provider, Shell parentShell);
}
