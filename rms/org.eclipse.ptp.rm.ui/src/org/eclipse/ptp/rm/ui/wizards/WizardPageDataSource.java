/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.ptp.rm.ui.utils.DataSource;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public abstract class WizardPageDataSource extends DataSource {
	private final AbstractConfigurationWizardPage fPage;
	private IResourceManagerComponentConfiguration fComponentConfig;

	protected WizardPageDataSource(AbstractConfigurationWizardPage page) {
		fPage = page;
	}

	@Override
	protected void setErrorMessage(ValidationException e) {
		fPage.setErrorMessage(e.getLocalizedMessage());
		fPage.setPageComplete(false);
	}

	@Override
	protected void update() {
		fPage.updateControls();
	}

	/**
	 * @since 2.0
	 */
	public IResourceManagerComponentConfiguration getConfiguration() {
		return fComponentConfig;
	}

	/**
	 * @since 2.0
	 */
	public void setConfiguration(IResourceManagerComponentConfiguration config) {
		fComponentConfig = config;
	}
}
