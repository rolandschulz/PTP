/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.launch.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

/**
 * A modified version of {@link org.eclipse.debug.ui.EnvironmentTab}, where the
 * "append" and "replace" options are always disabled, and set to "append". 
 * @author Daniel Felix Ferber
 */
public class EnvironmentTab extends org.eclipse.debug.ui.EnvironmentTab {
	@Override
	protected void updateAppendReplace() {
		appendEnvironment.setEnabled(false);
		replaceEnvironment.setEnabled(false);	
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Ensure that the "append"/"replace" attribute is always set to "append", 
		 * ignoring whatever was set as default by the original implementation.
		 */
		super.setDefaults(configuration);
		configuration.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		/*
		 * Ensure that the "append"/"replace" controls are always shown as "append", 
		 * ignoring whatever was shown by the original implementation.
		 */
		appendEnvironment.setSelection(true);
        replaceEnvironment.setSelection(false);
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Ensure that the "append"/"replace" attribute is always set to "append", 
		 * ignoring whatever was set by the original implementation.
		 */
		super.performApply(configuration);
		configuration.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}
}
