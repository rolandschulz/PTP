/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.wizards;

/**
 * Proxy between extension point and new project wizard to create the wizard with additional settings
 */
public class NewSyncProjectWizardProxy extends NewSyncProjectWizard {
	public NewSyncProjectWizardProxy() {
		super(WizardMode.NEW);
	}
}
