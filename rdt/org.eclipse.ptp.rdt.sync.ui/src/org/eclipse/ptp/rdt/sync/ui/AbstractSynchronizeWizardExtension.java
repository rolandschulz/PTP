/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;


/**
 * Must be extended by extensions to the syncWizardExtension extension point.
 * 
 */
public abstract class AbstractSynchronizeWizardExtension implements ISynchronizeWizardExtension {
	public ISynchronizeWizardExtensionDescriptor fDescriptor;

	public AbstractSynchronizeWizardExtension(ISynchronizeWizardExtensionDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	@Override
	public String getNature() {
		return fDescriptor.getNature();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardPageDescriptor#getPage()
	 */
	@Override
	public ISynchronizeWizardExtension getWizardExtension() {
		return this;
	}
}
