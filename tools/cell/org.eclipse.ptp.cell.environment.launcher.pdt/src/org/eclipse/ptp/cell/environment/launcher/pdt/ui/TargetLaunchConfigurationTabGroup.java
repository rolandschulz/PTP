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
package org.eclipse.ptp.cell.environment.launcher.pdt.ui;

//import org.eclipse.cdt.launch.internal.ui.MigratingCEnvironmentTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.cell.environment.launcher.cellbe.ui.TargetTab;
import org.eclipse.ptp.remotetools.environment.launcher.ui.EnhancedSynchronizeTab;
import org.eclipse.ptp.remotetools.environment.launcher.ui.LauncherExecutionTab;


public class TargetLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new CMainTab(true),
				new TargetTab(),
				new LauncherExecutionTab(),
				new EnhancedSynchronizeTab(),
				new PdtEnvironmentTab(),
				new PdtSystemEnvironmentTab(),
				//new SourceLookupTab(),
				new CommonTab() 
			};
		setTabs(tabs);
	}


}
