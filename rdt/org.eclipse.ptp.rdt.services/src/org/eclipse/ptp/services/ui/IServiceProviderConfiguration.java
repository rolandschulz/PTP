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
package org.eclipse.ptp.services.ui;

import org.eclipse.ptp.services.core.IServiceProvider;

public interface IServiceProviderConfiguration {
	/**
	 * Configure the service provider, which typically launches a dialog/wizard to allow the 
	 * service provider to be configured (may include setting up and associating any required connections)
	 * 
	 * @param provider
	 */
	public void configureServiceProvider(IServiceProvider provider);
}
