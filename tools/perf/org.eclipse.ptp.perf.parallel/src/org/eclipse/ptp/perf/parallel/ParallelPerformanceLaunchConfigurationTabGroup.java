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
package org.eclipse.ptp.perf.parallel;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.PArgumentsTab;
import org.eclipse.ptp.launch.ui.PCommonTab;
import org.eclipse.ptp.launch.ui.PDebuggerTab;
import org.eclipse.ptp.launch.ui.ResourcesTab;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.ui.AbstractPerformanceConfigurationTab;
import org.eclipse.ptp.perf.ui.PerformanceAnalysisTab;

/**
 * Defines the tab group in the performance analysis launch configuration system using the PTP launcher
 */
public class ParallelPerformanceLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	/**
	 * Creates the tabs used by the performance launch configuration system
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractPerformanceConfigurationTab> perfTabs=Activator.getPerfTabs();
		ILaunchConfigurationTab tabs[]=new ILaunchConfigurationTab[7+perfTabs.size()];
		tabs[0]=new ParallelPerfRecompMainTab();
		tabs[1]=new PArgumentsTab();
		tabs[2]=new EnvironmentTab();
		tabs[3]=new ResourcesTab();
		tabs[4]=new PerformanceAnalysisTab(true);
		tabs[5]=new PDebuggerTab(false);
		tabs[6]=new PCommonTab();
		Iterator<AbstractPerformanceConfigurationTab> perfIt=perfTabs.iterator();
		int tabDex=7;
		while(perfIt.hasNext())
		{
			tabs[tabDex]=perfIt.next();
		}
		
//    		ILaunchConfigurationTab tabs[] = {
//    			new ParallelPerfRecompMainTab(), 
//            	new PArgumentsTab(),
//           		new EnvironmentTab(), 
//           		new ResourcesTab(),
//        		new PerformanceAnalysisTab(false),
//        		//new TAUAnalysisTab(false),
//        		new PDebuggerTab(false),
//        		new PCommonTab()
//        		};
    		setTabs(tabs);
	}    
}