/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.launch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.launch.variables.ETFWVariableMap;
import org.eclipse.ptp.launch.ui.extensions.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;

/**
 * Extends JAXBControllerLaunchConfigurationTab with specific changes for ETFw. The LC Map needs to be initialized from the
 * workflow's variable map, not the resource managers.
 * 
 * @author Chris Navarro
 * 
 */
public class ETFWParentLaunchConfigurationTab extends JAXBControllerLaunchConfigurationTab {
	private final ETFWVariableMap variableMap;

	public ETFWParentLaunchConfigurationTab(ILaunchController control, IProgressMonitor monitor, ETFWVariableMap variableMap)
			throws Throwable {
		super(control, monitor);
		this.variableMap = variableMap;
	}

	@Override
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		try {
			// This lets us differentiate keys from the old ETFW so they can work from one Profiling Tab
			getVariableMap().initialize(variableMap, getJobControl().getControlId());
			getUpdateHandler().clear();
			getVariableMap().updateFromConfiguration(configuration);
			delegate = RemoteServicesDelegate.getDelegate(getJobControl().getRemoteServicesId(), getJobControl()
					.getConnectionName(),
					getProgressMonitor());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	@Override
	public void relink() {
		// TODO do we need this for ETFw?
		// To provide this, we'd probably need the parent to contain the list of JAXBDynamicLaunchConfigurationTabs, which currently
		// it does not so it could get at the selected tab and pass in the controller tag.
	}

}
