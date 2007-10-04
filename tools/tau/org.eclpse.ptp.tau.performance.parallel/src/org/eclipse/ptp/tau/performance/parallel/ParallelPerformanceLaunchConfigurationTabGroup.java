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
package org.eclipse.ptp.tau.performance.parallel;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.PArgumentsTab;
import org.eclipse.ptp.launch.ui.PCommonTab;
import org.eclipse.ptp.launch.ui.PDebuggerTab;
import org.eclipse.ptp.launch.ui.ParallelTab;
import org.eclipse.ptp.tau.performance.tau.TAUAnalysisTab;
import org.eclipse.ptp.tau.performance.ui.PerformanceAnalysisTab;

/**
 * Defines the tab group in the performance analysis launch configuration system using the PTP launcher
 */
public class ParallelPerformanceLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	/**
	 * Creates the tabs used by the performance launch configuration system
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    		ILaunchConfigurationTab tabs[] = {
    			new ParallelPerfRecompMainTab(), 
            	new PArgumentsTab(),
           		new EnvironmentTab(), 
           		new ParallelTab(),
        		new PerformanceAnalysisTab(false),
        		new TAUAnalysisTab(false),
        		new PDebuggerTab(false),
        		new PCommonTab()
        		};
    		setTabs(tabs);
	}    
}