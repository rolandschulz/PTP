/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.launch;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.internal.etfw.jaxb.data.ToolPaneType;
import org.eclipse.ptp.launch.ui.extensions.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.JAXBDynamicLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends JAXBControllerLaunchConfigurationTab with specific changes for ETFw. The LC Map needs to be initialized from the
 * workflow's variable map, not the resource managers.
 * 
 * @author Chris Navarro
 * 
 */
public class ETFWParentLaunchConfigurationTab extends JAXBControllerLaunchConfigurationTab {
	private final IVariableMap variableMap;

	public ETFWParentLaunchConfigurationTab(ILaunchController control, ILaunchConfigurationDialog dialog, IProgressMonitor monitor,
			List<ToolPaneType> toolPanes, IVariableMap variableMap)
			throws Throwable {
		super(control, dialog, monitor);

		this.variableMap = variableMap;
		getControllers().clear();
		for (ToolPaneType toolTab : toolPanes) {
			JAXBDynamicLaunchConfigurationTab dynamicTab = new JAXBDynamicLaunchConfigurationTab(this.getJobControl(),
					toolTab.getOptionPane(), this);
			dynamicTab.getController().setShowViewConfig(false);
			dynamicTab.setCheckCycles(false);
			addDynamicTab(dynamicTab);
		}
	}

	@Override
	public void createControl(Composite parent, String id) throws CoreException {
		super.createControl(parent, id);
	}

	@Override
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		// This prevents the parent from clearing the LC map and initializing it with the fControl RM map
		this.voidRMConfig = true;
		try {
			// Initialize the map with ETFw's RM map
			getVariableMap().initialize(variableMap, getJobControl().getControlId());
			getUpdateHandler().clear();
			getVariableMap().updateFromConfiguration(configuration);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		RMLaunchValidation validation = super.initializeFrom(configuration);
		return validation;
	}

	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		RMLaunchValidation validation = super.performApply(configuration);
		return validation;
	}

	@Override
	public void relink() {
		// Some of the jaxb classes need to be re-worked for this to be implemented because their are target configuration specifics
		// in this call hierarchy
		super.relink();
	}

	public IVariableMap getRMVariableMap() {
		return this.variableMap;
	}

}
