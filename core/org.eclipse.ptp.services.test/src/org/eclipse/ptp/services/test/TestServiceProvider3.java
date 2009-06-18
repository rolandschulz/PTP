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
package org.eclipse.ptp.services.test;

import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ui.IMemento;

public class TestServiceProvider3 extends ServiceProvider {

	public TestServiceProvider3() {
	}

	public boolean isConfigured() {
		return true;
	}

	public void restoreState(IMemento providerMemento) {
	}

	public void saveState(IMemento providerMemento) {
	}
}
