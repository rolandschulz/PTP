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
package org.eclipse.ptp.cell.debug.launch.ui.tabs;

//import java.util.Map;

import org.eclipse.cdt.launch.ui.CDebuggerTab;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class CellDebugTab extends CDebuggerTab {

	public CellDebugTab(boolean attachMode) {
		super(attachMode);
		// TODO Auto-generated constructor stub
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		/* Do not propagate individual launcher prefs to global prefs
		PreferenceConstants preferences = PreferenceConstants.getInstance();
		
		Map attr;
		try {
			attr = config.getAttributes();
			Object attribute = null;
			
			if (attr!=null && attr.size() > 0) {
				if ( (attribute = attr.get(IGDBServerMILaunchConfigurationConstants.ATTR_PORT)) != null ) {
					preferences.setSIMULATOR_DEBUG_GDBSPORT(new Integer((String)attribute).intValue());
				}
				attribute = null;
				if ( (attribute = attr.get(IGDBServerMILaunchConfigurationConstants.ATTR_HOST)) != null ) {
					preferences.setSIMULATOR_RUNTIME_IPADDR((String) attribute);
				}
				attribute = null;
				if ( (attribute = attr.get(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME)) != null ) {
					preferences.setDEBUG_GDBBIN(new Path((String) attribute));
				}
			}
		} catch (CoreException e) {
			
			e.printStackTrace();
		}
		*/
	}
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
		super.setDefaults(configuration);
		//setPPUGDBServerDefaults(configuration);
		//setSPUGDBServerDefaults(configuration);
	}
	
	private void setPPUGDBServerDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		PreferenceConstants preferences = PreferenceConstants.getInstance();
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID,"org.eclipse.cdt.debug.mi.core.GDBServerCDebugger");
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL,"mi");
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP,true);
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT,(new Integer(preferences.getSIMULATOR_DEBUG_GDBSPORT())).toString());
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST,preferences.getSIMULATOR_RUNTIME_IPADDR());
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,false);
		configuration.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME,preferences.getDEBUG_GDBBIN().toOSString());
		*/
	}
	
	private void setSPUGDBServerDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}
}
