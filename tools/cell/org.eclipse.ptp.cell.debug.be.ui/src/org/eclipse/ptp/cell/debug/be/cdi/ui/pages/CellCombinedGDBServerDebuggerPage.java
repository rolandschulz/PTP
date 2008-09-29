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
package org.eclipse.ptp.cell.debug.be.cdi.ui.pages;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.cell.debug.be.cdi.ui.pages.BEUIMessages;
import org.eclipse.ptp.cell.debug.be.preferences.BEDebugPreferencesConstants;
import org.eclipse.ptp.cell.debug.cdi.ui.pages.StandardCellGDBServerDebuggerPage;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchConstants;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class CellCombinedGDBServerDebuggerPage extends StandardCellGDBServerDebuggerPage {

	public static String ATTR_DEBUGGER_ID = "org.eclipse.ptp.cell.debug.cdi.combinedGDBServerCDIDebugger"; //$NON-NLS-1$
	
	public String ATTR_DEBUGGER_BIN = BEUIMessages.getString("CellCombinedGDBServerDebuggerPage.0"); //$NON-NLS-1$
	
	public CellCombinedGDBServerDebuggerPage() {
		super();
		
	}
	
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL,"mi"); //$NON-NLS-1$
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_CAPABLE, 1);
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_CFG, "Cell BE gdbserver" ); //$NON-NLS-1$
		super.setDefaults(configuration);
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,false);
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME,BEDebugPreferencesConstants.getBE_GDB_BIN_VALUE());
		
	}

}
