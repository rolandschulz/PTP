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
package org.eclipse.ptp.etfw.parallel;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.ptp.etfw.Activator;
import org.eclipse.ptp.etfw.ui.AbstractToolConfigurationTab;
import org.eclipse.ptp.etfw.ui.ParametricParameterTab;
import org.eclipse.ptp.etfw.ui.ExternalToolSelectionTab;
import org.eclipse.ptp.launch.ui.ArgumentsTab;
import org.eclipse.ptp.launch.ui.DebuggerTab;
import org.eclipse.ptp.launch.ui.ResourcesTab;

/**
 * Defines the tab group in the performance analysis launch configuration system using the PTP launcher
 */
public class ParallelToolLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	/**
	 * Creates the tabs used by the performance launch configuration system
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		
		int numStatTabs=9;
		
		ArrayList<AbstractToolConfigurationTab> perfTabs=Activator.getPerfTabs();
		ILaunchConfigurationTab tabs[]=new ILaunchConfigurationTab[numStatTabs+perfTabs.size()];
		tabs[0]=new ParallelToolRecompMainTab();
		tabs[1]=new ResourcesTab();
		tabs[2]=new DebuggerTab(false);
		tabs[3]=new ArgumentsTab();
		tabs[4]=new SourceLookupTab();
		tabs[5]=new EnvironmentTab();
		tabs[6]=new CommonTab();
		tabs[7]=new ExternalToolSelectionTab(true);
		tabs[8]=new ParametricParameterTab(true);
		
		Iterator<AbstractToolConfigurationTab> perfIt=perfTabs.iterator();
		int tabDex=numStatTabs;
		while(perfIt.hasNext())
		{
			tabs[tabDex]=perfIt.next();
		}
		
//    		ILaunchConfigurationTab tabs[] = {
//    			new ParallelToolRecompMainTab(), 
//            	new PArgumentsTab(),
//           		new EnvironmentTab(), 
//           		new ResourcesTab(),
//        		new ExternalToolSelectionTab(false),
//        		//new TAUAnalysisTab(false),
//        		new PDebuggerTab(false),
//        		new PCommonTab()
//        		};
    		setTabs(tabs);
	}    
}