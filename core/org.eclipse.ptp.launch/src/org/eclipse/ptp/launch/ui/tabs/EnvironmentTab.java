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
package org.eclipse.ptp.launch.ui.tabs;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.launch.internal.messages.Messages;

/**
 * A modified version of {@link org.eclipse.debug.ui.EnvironmentTab}, where the
 * "append" and "replace" options are always disabled, and set to "append".
 * 
 * @author Daniel Felix Ferber
 */
public class EnvironmentTab extends org.eclipse.debug.ui.EnvironmentTab {
	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.launch.applicationLaunch.environmentTab"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.EnvironmentTab#updateAppendReplace()
	 */
	@Override
	protected void updateAppendReplace() {
		appendEnvironment.getParent().setToolTipText(Messages.EnvironmentTab_Tool_Tip);
		appendEnvironment.setEnabled(false);
		replaceEnvironment.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.EnvironmentTab#setDefaults(org.eclipse.debug.core
	 * .ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Ensure that the "append"/"replace" attribute is always set to
		 * "append", ignoring whatever was set as default by the original
		 * implementation.
		 */
		super.setDefaults(configuration);
		configuration.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.EnvironmentTab#initializeFrom(org.eclipse.debug.
	 * core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		/*
		 * Ensure that the "append"/"replace" controls are always shown as
		 * "append", ignoring whatever was shown by the original implementation.
		 */
		appendEnvironment.setSelection(true);
		replaceEnvironment.setSelection(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.EnvironmentTab#performApply(org.eclipse.debug.core
	 * .ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Ensure that the "append"/"replace" attribute is always set to
		 * "append", ignoring whatever was set by the original implementation.
		 */
		super.performApply(configuration);
		configuration.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}
}
