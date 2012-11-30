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
package org.eclipse.ptp.launch.ui.tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public class DebuggerTab extends AbstractDebuggerTab {
	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.launch.applicationLaunch.debuggerTab"; //$NON-NLS-1$

	protected final boolean fAttachMode;
	protected Button fStopInMain;
	protected Button fAttachButton;

	public DebuggerTab(boolean attachMode) {
		fAttachMode = attachMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.internal.ui.AbstractDebuggerTab#createControl( org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout layout = new GridLayout(2, true);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		comp.setLayoutData(gd);

		createDebuggerCombo(comp, (fAttachMode) ? 1 : 2);
		createOptionsComposite(comp);
		createDebuggerGroup(comp, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.internal.ui.AbstractDebuggerTab#initializeFrom (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		setInitializing(true);
		super.initializeFrom(config);
		try {
			/*
			 * Only set default debugger if there is a remote connection.
			 */
			String id = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, EMPTY_STRING);
			loadDebuggerComboBox(config, id, LaunchUtils.getRemoteServicesId(config) == null);
			initializeCommonControls(config);
		} catch (CoreException e) {
		}
		setInitializing(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.AbstractDebuggerTab#activated(org.eclipse.debug .core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		setInitializing(true);
		super.activated(workingCopy);
		try {
			/*
			 * Only set default debugger if there is a resource manager selected.
			 */
			String id = workingCopy.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, EMPTY_STRING);
			loadDebuggerComboBox(workingCopy, id, LaunchUtils.getRemoteServicesId(workingCopy) == null);
			initializeCommonControls(workingCopy);
		} catch (CoreException e) {
		}
		setInitializing(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.internal.ui.AbstractDebuggerTab#isValid(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (!validateDebuggerConfig(config)) {
			return false;
		}
		IPDebugConfiguration debugConfig = getDebugConfig();
		String mode = fAttachMode ? IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH
				: IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		if (!debugConfig.supportsMode(mode)) {
			setErrorMessage(NLS.bind(Messages.DebuggerTab_Mode_not_supported, new Object[] { mode }));
			return false;
		}
		if (super.isValid(config) == false) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.internal.ui.AbstractDebuggerTab#performApply(org
	 * .eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		if (fAttachMode) {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, fStopInMain.getSelection());
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.internal.ui.AbstractDebuggerTab#setDefaults(org
	 * .eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		if (fAttachMode) {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
		} else {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN,
					IPTPLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT);
		}
	}

	/**
	 * Create a composite to display debugger options.
	 * 
	 * @param parent
	 */
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
			fStopInMain = createCheckButton(optionsComp, Messages.DebuggerTab_Stop_at_main_on_startup);
			GridData data = new GridData();
			data.horizontalAlignment = GridData.BEGINNING;
			fStopInMain.setLayoutData(data);
			fStopInMain.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!isInitializing()) {
						updateLaunchConfigurationDialog();
					}
				}
			});
		}
	}

	/**
	 * Initialize the controls.
	 * 
	 * @param config
	 */
	protected void initializeCommonControls(ILaunchConfiguration config) {
		try {
			if (!fAttachMode) {
				fStopInMain.setSelection(config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN,
						IPTPLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT));
			}
		} catch (CoreException e) {
		}
	}

	/**
	 * Load the debugger combo with installed debuggers.
	 * 
	 * @param config
	 * @param selection
	 */
	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection, boolean noDefault) {
		IPDebugConfiguration[] debugConfigs;
		debugConfigs = PTPDebugCorePlugin.getDefault().getDebugConfigurations();
		Arrays.sort(debugConfigs, new Comparator<IPDebugConfiguration>() {
			public int compare(IPDebugConfiguration ic1, IPDebugConfiguration ic2) {
				return ic1.getName().compareTo(ic2.getName());
			}
		});
		List<IPDebugConfiguration> list = new ArrayList<IPDebugConfiguration>();
		String mode;
		if (fAttachMode) {
			mode = IPTPLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH;
		} else {
			mode = IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
		}
		String defaultSelection = selection;
		for (int i = 0; i < debugConfigs.length; i++) {
			if (debugConfigs[i].supportsMode(mode)) {
				list.add(debugConfigs[i]);
				// select first exact matching debugger for requested selection
				if (!noDefault && defaultSelection.equals("")) { //$NON-NLS-1$
					defaultSelection = debugConfigs[i].getID();
				}
			}
		}
		// if no selection meaning nothing in config the force initdefault on
		// tab
		setInitializeDefault(selection.equals("") ? true : false); //$NON-NLS-1$
		loadDebuggerCombo(list.toArray(new IPDebugConfiguration[list.size()]), defaultSelection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.internal.ui.AbstractDebuggerTab# updateComboFromSelection()
	 */
	@Override
	protected void updateComboFromSelection() {
		super.updateComboFromSelection();
		initializeCommonControls(getLaunchConfiguration());
	}

	/**
	 * Validate the debugger configuration.
	 * 
	 * @param config
	 * @return
	 */
	protected boolean validateDebuggerConfig(ILaunchConfiguration config) {
		IPDebugConfiguration debugConfig = getDebugConfig();
		if (debugConfig == null) {
			setErrorMessage(Messages.DebuggerTab_No_debugger_available);
			return false;
		}
		return true;
	}

	/**
	 * Check that the selected debugger can be used for this launch
	 * 
	 * FIXME: This needs to check the debugger supports the platform being managed by the resource manager. The RM will need to
	 * provide an interface to get this information.
	 * 
	 * @param config
	 * @param debugConfig
	 * @return
	 */
	protected boolean validatePlatform(ILaunchConfiguration config, IPDebugConfiguration debugConfig) {
		return true;
	}
}
