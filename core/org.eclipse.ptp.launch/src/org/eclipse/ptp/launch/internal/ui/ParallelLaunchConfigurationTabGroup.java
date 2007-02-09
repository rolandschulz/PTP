/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.internal.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.ptp.launch.ui.PArgumentsTab;
import org.eclipse.ptp.launch.ui.PCommonTab;
import org.eclipse.ptp.launch.ui.PDebuggerTab;
import org.eclipse.ptp.launch.ui.PMainTab;
import org.eclipse.ptp.launch.ui.ParallelTab;

/**
 * 
 */
public class ParallelLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    		ILaunchConfigurationTab tabs[] = {
        		new PMainTab(), 
        		new ParallelTab(),
        		new PDebuggerTab(false),
        		new PArgumentsTab(),
        		new SourceLookupTab(),
        		new EnvironmentTab(), 
        		new PCommonTab() };
    		setTabs(tabs);
	}    
}