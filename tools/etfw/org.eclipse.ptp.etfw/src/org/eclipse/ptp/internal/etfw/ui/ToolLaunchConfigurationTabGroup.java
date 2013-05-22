/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 *    Roland Grunberg - tab creation from extension point
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;

/**
 * Defines the tab group in the performance analysis launch configuration system using the CDT launcher
 */
public class ToolLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup implements IToolLaunchConfigurationConstants {
	/**
	 * Creates the tabs used by the performance launch configuration system
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab [] perfTabs = ETFWUtils.getPerfTabs().toArray(new ILaunchConfigurationTab [0]);
		setTabs(perfTabs);
	}
}