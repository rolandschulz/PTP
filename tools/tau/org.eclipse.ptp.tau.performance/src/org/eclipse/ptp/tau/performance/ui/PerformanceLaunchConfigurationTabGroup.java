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
 ****************************************************************************/
package org.eclipse.ptp.tau.performance.ui;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.ptp.tau.performance.internal.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.tau.performance.tau.TAUAnalysisTab;

/**
 * Defines the tab group in the performance analysis launch configuration system using the CDT launcher
 */
public class PerformanceLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup implements IPerformanceLaunchConfigurationConstants{
	/**
	 * Creates the tabs used by the performance launch configuration system
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		
		ILaunchConfigurationTab tabs[] = {
				new PerfRecompMainTab(true), 
				new CArgumentsTab(), 
				new EnvironmentTab(),
				new PerformanceAnalysisTab(true), 
				new TAUAnalysisTab(true),
				new CDebuggerTab(false),
				new SourceLookupTab(), 
				new CommonTab() 
		};
		setTabs(tabs);
	}
}