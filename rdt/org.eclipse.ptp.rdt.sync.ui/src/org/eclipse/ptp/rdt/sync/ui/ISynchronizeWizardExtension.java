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
package org.eclipse.ptp.rdt.sync.ui;

import org.eclipse.jface.wizard.WizardPage;

/*
 * Implemented by clients wishing to add pages to the synchronized project's conversion wizard.
 */
public interface ISynchronizeWizardExtension extends ISynchronizeWizardExtensionDescriptor {
	/**
	 * Create pages added to end of conversion wizard
	 * 
	 * @return array of wizard pages
	 */
	public WizardPage createConvertProjectWizardPage();

	/**
	 * Called when the wizard exits and after the project has been created or converted.
	 */
	public void performFinish();
}
