/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cldt.launch.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.ICDescriptor;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.debug.core.CDebugCorePlugin;
import org.eclipse.cldt.debug.core.ICDebugConfiguration;
import org.eclipse.cldt.debug.core.IFDTLaunchConfigurationConstants;
import org.eclipse.cldt.launch.internal.ui.AbstractDebuggerTab;
import org.eclipse.cldt.launch.internal.ui.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

public class CoreFileDebuggerTab extends AbstractDebuggerTab {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		WorkbenchHelp.setHelp(getControl(), ILaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);
		GridLayout topLayout = new GridLayout(1, false);
		comp.setLayout(topLayout);
		createDebuggerCombo(comp, 1);
		createDebuggerGroup(comp, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IFDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
							IFDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getDebugConfig() != null) {
			config.setAttribute(IFDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID());
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(IFDTLaunchConfigurationConstants.ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP, (Map)null);
			} else {
				dynamicTab.performApply(config);
			}
		}
	}

	public void initializeFrom(ILaunchConfiguration config) {
		setInitializing(true);
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(IFDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			loadDebuggerComboBox(config, id);
		} catch (CoreException e) {
		}
		setInitializing(false);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.internal.ui.AbstractDebuggerTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		setInitializing(true);
		try {
			String id = workingCopy.getAttribute(IFDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""); //$NON-NLS-1$
			loadDebuggerComboBox(workingCopy, id);
		} catch (CoreException e) {
		}
		setInitializing(false);
		super.activated(workingCopy);
	}
	
	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			return false;
		}
		if (super.isValid(config) == false) {
			return false;
		}
		return true;
	}

	protected boolean validateDebuggerConfig(ILaunchConfiguration config) {
		ICDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			setErrorMessage(LaunchMessages.getString("CoreFileDebuggerTab.No_debugger_available")); //$NON-NLS-1$
			return false;
		}
		if (!validatePlatform(config, debugConfig)) {
			setErrorMessage(LaunchMessages.getString("CoreFileDebuggerTab.platform_is_not_supported")); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		String projectPlatform = getProjectPlatform(config);
		String defaultSelection = null;
		List list = new ArrayList();
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(IFDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
				if (validatePlatform(config, debugConfigs[i])) {
					list.add(debugConfigs[i]);
					// select first exact matching debugger for platform or
					// requested selection
					String debuggerPlatform = debugConfigs[i].getPlatform();
					if (defaultSelection == null && debuggerPlatform.equalsIgnoreCase(projectPlatform)) { //$NON-NLS-1$
						defaultSelection = debugConfigs[i].getID();
					}
				}
				if (selection.equals(debugConfigs[i].getID())) {
					defaultSelection = debugConfigs[i].getID();
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on
		// tab
		setInitializeDefault(selection.equals("") ? true : false); //$NON-NLS-1$
		loadDebuggerCombo((ICDebugConfiguration[])list.toArray(new ICDebugConfiguration[list.size()]), defaultSelection);
	}

	protected boolean validatePlatform(ILaunchConfiguration config, ICDebugConfiguration debugConfig) {
		String projectPlatform = getProjectPlatform(config);
		String debuggerPlatform = debugConfig.getPlatform();
		return (projectPlatform.equals("*") || debuggerPlatform.equals("*") || debuggerPlatform.equalsIgnoreCase(projectPlatform)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String getProjectPlatform(ILaunchConfiguration config) {
		ICElement ce = getContext(config, null);
		String projectPlatform = "*"; //$NON-NLS-1$
		if (ce != null) {
			try {
				ICDescriptor descriptor = FortranCorePlugin.getDefault().getCProjectDescription(ce.getCProject().getProject(), false);
				if (descriptor != null) {
					projectPlatform = descriptor.getPlatform();
				}
			} catch (Exception e) {
			}
		}
		return projectPlatform;
	}
}