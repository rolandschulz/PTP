/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.launch.ui.tabs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.ui.LaunchImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public abstract class AbstractDebuggerTab extends LaunchConfigurationTab {
	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
	protected IPDebugConfiguration fCurrentDebugConfig;

	// Dynamic Debugger UI widgets
	protected ILaunchConfigurationTab fDynamicTab;
	protected Composite fDynamicTabHolder;
	private boolean fInitDefaults;
	private Combo fDCombo;
	private boolean fIsInitializing = false;
	private boolean fPageUpdated;

	protected void setDebugConfig(IPDebugConfiguration config) {
		fCurrentDebugConfig = config;
	}

	protected IPDebugConfiguration getDebugConfig() {
		return fCurrentDebugConfig;
	}

	protected ILaunchConfigurationTab getDynamicTab() {
		return fDynamicTab;
	}

	protected void setDynamicTab(ILaunchConfigurationTab tab) {
		fDynamicTab = tab;
	}

	protected Composite getDynamicTabHolder() {
		return fDynamicTabHolder;
	}

	protected void setDynamicTabHolder(Composite tabHolder) {
		fDynamicTabHolder = tabHolder;
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
		return fWorkingCopy;
	}

	@Override
	public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		super.setLaunchConfiguration(launchConfiguration);
		setLaunchConfigurationWorkingCopy(null);
	}

	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}

	/**
	 * Overridden here so that any error message in the dynamic UI gets returned.
	 * 
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		ILaunchConfigurationTab tab = getDynamicTab();
		if ((super.getErrorMessage() != null) || (tab == null)) {
			return super.getErrorMessage();
		}
		return tab.getErrorMessage();
	}

	/**
	 * Notification that the user changed the selection of the Debugger.
	 */
	protected void handleDebuggerChanged() {
		loadDynamicDebugArea();

		// always set the newly created area with defaults
		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
		if (getDynamicTab() == null) {
			// remove any debug specfic args from the config
			if (wc == null) {
				if (getLaunchConfiguration().isWorkingCopy()) {
					wc = (ILaunchConfigurationWorkingCopy) getLaunchConfiguration();
				}
			}
			if (wc != null) {
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS, (String) null);
			}
		} else {
			if (wc == null) {
				try {
					if (getLaunchConfiguration().isWorkingCopy()) {
						setLaunchConfigurationWorkingCopy((ILaunchConfigurationWorkingCopy) getLaunchConfiguration());
					} else {
						setLaunchConfigurationWorkingCopy(getLaunchConfiguration().getWorkingCopy());
					}
					wc = getLaunchConfigurationWorkingCopy();
				} catch (CoreException e) {
					return;
				}
			}
			if (initDefaults()) {
				getDynamicTab().setDefaults(wc);
			}
			setInitializeDefault(false);
			getDynamicTab().initializeFrom(wc);
		}
	}

	/**
	 * Show the contributed piece of UI that was registered for the debugger id of the currently selected debugger.
	 */
	protected void loadDynamicDebugArea() {
		// Dispose of any current child widgets in the tab holder area
		Control[] children = getDynamicTabHolder().getChildren();
		for (Control element : children) {
			element.dispose();
		}

		// Retrieve the dynamic UI for the current Debugger
		IPDebugConfiguration debugConfig = getConfigForCurrentDebugger();
		if (debugConfig == null) {
			setDynamicTab(null);
		} else {
			ILaunchConfigurationTab tab = null;
			try {
				tab = PTPDebugUIPlugin.getDefault().getDebuggerPage(debugConfig.getID());
			} catch (CoreException e) {
				PTPLaunchPlugin.errorDialog(Messages.AbstractDebuggerTab_ErrorLoadingDebuggerPage, e.getStatus());
			}
			setDynamicTab(tab);
		}
		setDebugConfig(debugConfig);
		if (getDynamicTab() == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		getDynamicTab().createControl(getDynamicTabHolder());
		getDynamicTab().getControl().setVisible(true);
		getDynamicTabHolder().layout(true);
	}

	abstract public void createControl(Composite parent);

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.activated(workingCopy);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.initializeFrom(config);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (getDebugConfig() != null) {
			config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, getDebugConfig().getID());
			ILaunchConfigurationTab dynamicTab = getDynamicTab();
			if (dynamicTab == null) {
				config.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS, (String) null);
			} else {
				dynamicTab.performApply(config);
			}
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		setLaunchConfigurationWorkingCopy(config);
		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			dynamicTab.setDefaults(config);
			setInitializeDefault(false);
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		if (getDebugConfig() == null) {
			setErrorMessage(Messages.AbstractDebuggerTab_No_debugger_available);
			return false;
		}

		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.isValid(config);
		}
		return true;
	}

	@Override
	public boolean canSave() {
		setErrorMessage(null);
		setMessage(null);
		if (getDebugConfig() == null) {
			setErrorMessage(Messages.AbstractDebuggerTab_No_debugger_available);
			return false;
		}

		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
			return dynamicTab.canSave();
		}
		return true;
	}

	protected void setInitializeDefault(boolean init) {
		fInitDefaults = init;
	}

	protected boolean initDefaults() {
		return fInitDefaults;
	}

	@Override
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_DEBUGGER_TAB);
	}

	public String getName() {
		return Messages.AbstractDebuggerTab_Debugger;
	}

	protected void createDebuggerCombo(Composite parent, int colspan) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colspan;
		comboComp.setLayoutData(gd);
		Label dlabel = new Label(comboComp, SWT.NONE);
		dlabel.setText(Messages.Launch_common_DebuggerColon);
		fDCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fDCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (!isInitializing() && event.type == SWT.Selection) {
					setInitializeDefault(true);
					updateComboFromSelection();
				}
			}
		});
	}

	protected void loadDebuggerCombo(IPDebugConfiguration[] debugConfigs, String current) {
		fDCombo.removeAll();
		int select = 0;
		for (int i = 0; i < debugConfigs.length; i++) {
			fDCombo.add(debugConfigs[i].getName());
			fDCombo.setData(Integer.toString(i), debugConfigs[i]);
			if (debugConfigs[i].getID().equalsIgnoreCase(current)) {
				select = i;
			}
		}

		fPageUpdated = false;
		if (select != -1) {
			fDCombo.select(select);
		}
		// The behaviour is undefined for if the callbacks should be triggered
		// for this, so force page update if needed.
		if (!fPageUpdated) {
			updateComboFromSelection();
		}
		fPageUpdated = false;
		getControl().getParent().layout(true);
	}

	protected void createDebuggerGroup(Composite parent, int colspan) {
		Group debuggerGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setText(Messages.DebuggerTab_Debugger_Options);
		setDynamicTabHolder(debuggerGroup);
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		getDynamicTabHolder().setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = colspan;
		getDynamicTabHolder().setLayoutData(gd);
	}

	protected void updateComboFromSelection() {
		fPageUpdated = true;
		handleDebuggerChanged();
		updateLaunchConfigurationDialog();
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	protected void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}

	/**
	 * Return the class that implements <code>ILaunchConfigurationTab</code> that is registered against the debugger id of the
	 * currently selected debugger.
	 */
	protected IPDebugConfiguration getConfigForCurrentDebugger() {
		int selectedIndex = fDCombo.getSelectionIndex();
		return (IPDebugConfiguration) fDCombo.getData(Integer.toString(selectedIndex));
	}

}
