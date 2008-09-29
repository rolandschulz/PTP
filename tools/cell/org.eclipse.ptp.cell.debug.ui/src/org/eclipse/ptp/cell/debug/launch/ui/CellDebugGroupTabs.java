/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.launch.ui;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.ptp.cell.debug.launch.ui.tabs.CellDebugTab;
import org.eclipse.ptp.cell.debug.launch.ui.tabs.CellTargetEnvironmentTab;




/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class CellDebugGroupTabs extends
		AbstractLaunchConfigurationTabGroup {

	public CellDebugGroupTabs() {
		super();
	}

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new CMainTab(true),
				new CellTargetEnvironmentTab(),
				new CArgumentsTab(),
				//new MigratingCEnvironmentTab(),
				new CellDebugTab(false),
				new SourceLookupTab(),
				new CommonTab() 
		};
		setTabs(tabs);
		
	}

}
