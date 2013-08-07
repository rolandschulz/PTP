/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *  Greg Watson - additional functionality to support launch configurations
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.launch;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.handlers.ControlStateListener;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonActionType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;

/**
 * Target Configuration Dynamic Tab interface. Corresponds to the "dynamic" element in the target configuration.
 * 
 * @since 1.1
 * 
 */
public interface IJAXBLaunchConfigurationTab {
	public TabControllerType getController();

	public ILaunchConfigurationDialog getLaunchConfigurationDialog();

	public Map<Object, IUpdateModel> getLocalWidgets();

	public IJAXBParentLaunchConfigurationTab getParent();

	public IRemoteConnection getRemoteConnection();

	public void run(ButtonActionType action) throws CoreException;

	public void setListeners(Collection<ControlStateListener> listeners);
}
