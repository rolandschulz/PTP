/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.ui.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.rm.ui.utils.DataSource;

public abstract class RMLaunchConfigurationDynamicTabDataSource extends DataSource {
	private final BaseRMLaunchConfigurationDynamicTab page;
	private ILaunchConfiguration configuration;
	private ILaunchConfigurationWorkingCopy configurationWorkingCopy;

	protected RMLaunchConfigurationDynamicTabDataSource(BaseRMLaunchConfigurationDynamicTab page) {
		this.page = page;
	}

	@Override
	protected void setErrorMessage(ValidationException e) {
		// page.setErrorMessage(e.getLocalizedMessage());
		// page.setValid(false);
	}

	@Override
	protected void update() {
		page.updateControls();
	}

	public void setConfiguration(ILaunchConfiguration configuration) {
		this.configuration = configuration;
	}

	public ILaunchConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy configurationWorkingCopy) {
		this.configurationWorkingCopy = configurationWorkingCopy;
	}

	public ILaunchConfigurationWorkingCopy getConfigurationWorkingCopy() {
		return configurationWorkingCopy;
	}
}
