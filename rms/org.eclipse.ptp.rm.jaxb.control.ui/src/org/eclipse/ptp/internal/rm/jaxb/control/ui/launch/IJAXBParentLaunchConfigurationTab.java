/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *  Greg Watson - improvements to support more launch configuration functionality
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.launch;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;

/**
 * Target Configuration Launch Tab interface. Corresponds to the "launch-tab" element in the target configuration.
 * 
 */
public interface IJAXBParentLaunchConfigurationTab {
	public IRemoteConnection getConnection();

	public ILaunchController getJobControl();

	public ILaunchConfigurationDialog getLaunchConfigurationDialog();

	public ScriptType getScript();

	public IUpdateHandler getUpdateHandler();

	public LCVariableMap getVariableMap();

	public boolean hasScript();

	public boolean isInitialized();
}
