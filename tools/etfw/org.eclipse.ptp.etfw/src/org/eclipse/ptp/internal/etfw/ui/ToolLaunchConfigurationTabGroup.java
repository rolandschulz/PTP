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
package org.eclipse.ptp.internal.etfw.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.ui.AbstractToolConfigurationTab;

/**
 * Defines the tab group in the performance analysis launch configuration system using the CDT launcher
 */
public class ToolLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup implements IToolLaunchConfigurationConstants {
	/**
	 * Creates the tabs used by the performance launch configuration system
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {

		final ArrayList<AbstractToolConfigurationTab> perfTabs = ETFWUtils.getPerfTabs();
		final ILaunchConfigurationTab tabs[] = new ILaunchConfigurationTab[7 + perfTabs.size()];
		tabs[0] = new ToolRecompMainTab(true);
		// tabs[1]=new CDebuggerTab(false);
		tabs[1] = new CArgumentsTab();
		tabs[2] = new EnvironmentTab();
		tabs[3] = new SourceLookupTab();
		tabs[4] = new CommonTab();
		tabs[5] = new ExternalToolSelectionTab(true);
		tabs[6] = new ParametricParameterTab(false);
		final Iterator<AbstractToolConfigurationTab> perfIt = perfTabs.iterator();
		int tabDex = 7;
		while (perfIt.hasNext())
		{
			tabs[tabDex] = perfIt.next();
			tabDex++;
		}

		// ILaunchConfigurationTab tabs[] = {
		// new ToolRecompMainTab(true),
		// new CArgumentsTab(),
		// new EnvironmentTab(),
		// new ExternalToolSelectionTab(true),
		// //new TAUAnalysisTab(true),
		//
		//
		// new CDebuggerTab(false),
		// new SourceLookupTab(),
		// new CommonTab()
		// };
		setTabs(tabs);
	}
}