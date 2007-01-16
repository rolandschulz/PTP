/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.launch.ui;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.launch.internal.ui.AbstractPDebuggerTab;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class PDebuggerTab extends AbstractPDebuggerTab {
	final protected boolean fAttachMode;

	protected Button fStopInMain;
	protected Button fAttachButton;

	public PDebuggerTab(boolean attachMode) {
		fAttachMode = attachMode;
	}
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		//PTPLaunchPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), IPTPLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB);
		GridLayout layout = new GridLayout(2, true);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		comp.setLayoutData(gd);

		createDebuggerCombo(comp, (fAttachMode) ? 1 : 2);
		createOptionsComposite(comp);
		createDebuggerGroup(comp, 2);
	}

	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		IPDebugConfiguration[] debugConfigs;
		String configPlatform = getPlatform(config);
		debugConfigs = PTPDebugCorePlugin.getDefault().getDebugConfigurations();
		Arrays.sort(debugConfigs, new Comparator() {
			public int compare(Object o1, Object o2) {
				IPDebugConfiguration ic1 = (IPDebugConfiguration)o1;
				IPDebugConfiguration ic2 = (IPDebugConfiguration)o2;
				return ic1.getName().compareTo(ic2.getName());
			}
		});
		List list = new ArrayList();
		String mode;
		if (fAttachMode) {
			mode = IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH;
		} else {
			mode = IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		}
		String defaultSelection = selection;
		for (int i = 0; i < debugConfigs.length; i++) {
			//hard code the sim2 id
			if (debugConfigs[i].getID().equals("org.eclipse.ptp.debug.external.sim2")) {
/*				if (!PTPCorePlugin.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.DEVELOPER_MODE)) {
					continue;
				}
*/			}			
			
			if (debugConfigs[i].supportsMode(mode)) {
				String debuggerPlatform = debugConfigs[i].getPlatform();
				if (validatePlatform(config, debugConfigs[i])) {
					list.add(debugConfigs[i]);
					// select first exact matching debugger for platform or requested selection
					if ((defaultSelection.equals("") && debuggerPlatform.equalsIgnoreCase(configPlatform))) {
						defaultSelection = debugConfigs[i].getID();
					}
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on tab
		setInitializeDefault(selection.equals("") ? true : false);
		loadDebuggerCombo((IPDebugConfiguration[])list.toArray(new IPDebugConfiguration[list.size()]), defaultSelection);
	}

	protected void updateComboFromSelection() {
		super.updateComboFromSelection();
		initializeCommonControls(getLaunchConfiguration());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		if (fAttachMode) {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, IPTPLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT);
		}
	}

	public void initializeFrom(ILaunchConfiguration config) {
		setInitializing(true);
		super.initializeFrom(config);
		try {
			String id = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "");
			loadDebuggerComboBox(config, id);
			initializeCommonControls(config);
		} catch (CoreException e) {
		}
		setInitializing(false);
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		if (fAttachMode) {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, fStopInMain.getSelection());
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			return false;
		}
		IPDebugConfiguration debugConfig = getDebugConfig();
		String mode = fAttachMode ? IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH : IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		if (!debugConfig.supportsMode(mode)) {
			setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PDebuggerTab.Mode_not_supported"), new String[]{mode}));
			return false;
		}
		if (super.isValid(config) == false) {
			return false;
		}
		return true;
	}

	protected boolean validatePlatform(ILaunchConfiguration config, IPDebugConfiguration debugConfig) {
		String configPlatform = getPlatform(config);
		String debuggerPlatform = debugConfig.getPlatform();
		return (debuggerPlatform.equals("*") || debuggerPlatform.equalsIgnoreCase(configPlatform));
	}

	protected boolean validateCPU(ILaunchConfiguration config, IPDebugConfiguration debugConfig) {
		IBinaryObject binaryFile = null;
		try {
			binaryFile = getBinary(config);
		} catch (CoreException e) {
			setErrorMessage(e.getLocalizedMessage());
		}
		String projectCPU = IPDebugConfiguration.CPU_NATIVE;
		if (binaryFile != null) {
			projectCPU = binaryFile.getCPU();
		}
		return debugConfig.supportsCPU(projectCPU);
	}

	protected IBinaryObject getBinary(ILaunchConfiguration config) throws CoreException {
		String programName = null;
		String projectName = null;
		try {
			projectName = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			programName = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, "");
		} catch (CoreException e) {
		}
		IPath exePath = new Path(programName);		
		if (projectName != null && !projectName.equals("")) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (!exePath.isAbsolute()) {
				exePath = project.getFile(exePath).getLocation();
			}
			ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project);
			for (int i = 0; i < parserRef.length; i++) {
				try {
					IBinaryParser parser = (IBinaryParser)parserRef[i].createExtension();
					IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);
					if (exe != null) {
						return exe;
					}
				} catch (ClassCastException e) {
				} catch (IOException e) {
				}
			}
		}
		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
		try {
			return (IBinaryObject)parser.getBinary(exePath);
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		return null;
	}

	protected boolean validateDebuggerConfig(ILaunchConfiguration config) {
		IPDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			setErrorMessage(LaunchMessages.getResourceString("PDebuggerTab.No_debugger_available"));
			return false;
		}
		if (!validatePlatform(config, debugConfig)) {
			setErrorMessage(LaunchMessages.getResourceString("PDebuggerTab.Platform_is_not_supported"));
			return false;
		}
		/**
		 * TODO: dun check cpu at this stage
		 */
		if (!validateCPU(config, debugConfig)) {
			setErrorMessage(LaunchMessages.getResourceString("PDebuggerTab.CPU_is_not_supported"));
			return false;
		}
		return true;
	}

	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	protected void createOptionsComposite(Composite parent) {
		Composite optionsComp = new Composite(parent, SWT.NONE);

		if (fAttachMode == true) {
			GridLayout layout = new GridLayout(1, false);
			optionsComp.setLayout(layout);
			optionsComp.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 1, 1));
		} else {
			GridLayout layout = new GridLayout(2, false);
			optionsComp.setLayout(layout);
			optionsComp.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 2, 1));
			fStopInMain = createCheckButton(optionsComp, LaunchMessages.getResourceString("PDebuggerTab.Stop_at_main_on_startup"));
			GridData data = new GridData();
			data.horizontalAlignment = GridData.BEGINNING;
			fStopInMain.setLayoutData(data);
			fStopInMain.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (!isInitializing()) {
						updateLaunchConfigurationDialog();
					}
				}
			});
		}
	}

	protected Shell getShell() {
		return super.getShell();
	}

	public void dispose() {
		super.dispose();
	}

	protected void initializeCommonControls(ILaunchConfiguration config) {
		try {
			if (!fAttachMode) {
				fStopInMain.setSelection(config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, IPTPLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT));
			}
		} catch (CoreException e) {
		}
	}
	protected void setInitializeDefault(boolean init) {
		super.setInitializeDefault(init);
	}
}
