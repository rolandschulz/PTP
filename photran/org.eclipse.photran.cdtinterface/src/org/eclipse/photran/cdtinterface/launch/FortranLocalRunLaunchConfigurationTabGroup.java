/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.launch;

import org.eclipse.cdt.launch.internal.ui.LocalRunLaunchConfigurationTabGroup;
import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class FortranLocalRunLaunchConfigurationTabGroup extends LocalRunLaunchConfigurationTabGroup
{
    /*@Override*/ public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
            new CMainTab(true) {
                /*@Override*/ protected void createExeFileGroup(Composite parent, int colSpan) {
                    super.createExeFileGroup(parent, colSpan);
                    fProgLabel.setText("Fortran Application:");
                }
            },
            new CArgumentsTab(),
            new EnvironmentTab(),
            new CDebuggerTab(false),
            new SourceLookupTab(),
            new CommonTab() 
        };
        setTabs(tabs);
    }
}
