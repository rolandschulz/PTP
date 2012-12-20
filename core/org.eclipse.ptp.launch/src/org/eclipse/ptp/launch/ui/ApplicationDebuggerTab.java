/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.launch.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

/**
 * @since 4.0
 */
public class ApplicationDebuggerTab extends DebuggerTab {
	/**
	 * @since 6.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.launch.applicationLaunch.debuggerTab"; //$NON-NLS-1$

	public ApplicationDebuggerTab() {
		super(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.DebuggerTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			// Reset sourcePathComputer in case it was modified by another tab. See bug 397019.
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, "org.eclipse.ptp.debug.core.sourcePathComputer");
			wc.doSave();
		} catch (CoreException e) {
		}
		super.initializeFrom(config);
	}
}
