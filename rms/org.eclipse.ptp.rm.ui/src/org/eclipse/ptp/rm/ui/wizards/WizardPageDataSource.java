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
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public abstract class WizardPageDataSource extends DataSource {
	private AbstractConfigurationWizardPage page;
	private IResourceManagerConfiguration config;

	protected WizardPageDataSource(AbstractConfigurationWizardPage page) {
		this.page = page;
	}

	@Override
	protected void setErrorMessage(ValidationException e) {
		page.setErrorMessage(e.getLocalizedMessage());
		page.setPageComplete(false);
	}

	@Override
	protected void update() {
		page.updateControls();
	}

	public IResourceManagerConfiguration getConfig() {
		return config;
	}

	public void setConfig(IResourceManagerConfiguration config) {
		this.config = config;
	}
}
