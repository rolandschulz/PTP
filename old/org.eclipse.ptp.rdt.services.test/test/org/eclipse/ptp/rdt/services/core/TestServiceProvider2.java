/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.services.core;

import org.eclipse.ptp.rdt.services.ui.IServiceProviderConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;

public class TestServiceProvider2 implements IServiceProvider, IServiceProviderConfiguration {

	public boolean isConfigured() {
		return true;
	}

	public String getId() {
		return "TestProvider2"; //$NON-NLS-1$
	}

	public String getName() {
		return "Test Provider 2"; //$NON-NLS-1$
	}

	public String getServiceId() {
		return "TestService1"; //$NON-NLS-1$
	}

	public void configureServiceProvider(IServiceProvider provider, Shell parentShell) {
	}

	public void restoreState(IMemento providerMemento) {
	}

	public void saveState(IMemento providerMemento) {
	}

	public String getConfigurationString() {
		return null;
	}
}
