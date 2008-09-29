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
package org.eclipse.ptp.cell.environment.cellsimulator.preferences.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.core.remote.RemoteConfigurationBean;
import org.eclipse.ptp.cell.preferences.ui.LabelFieldEditor;
import org.eclipse.ptp.cell.preferences.ui.SpacerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.2.1
 */
public class RemoteSimulatorPreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor simulatorBaseDirectory;

	private StringFieldEditor workDirectory;

	private StringFieldEditor kernelImagePath;

	private StringFieldEditor rootImagePath;

	public RemoteSimulatorPreferencePage() {
		super(Messages.RemoteSimulatorPreferencePage_Title, null, GRID);

		// Set the preference store for the preference page.
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		
		setTitle(Messages.RemoteSimulatorPreferencePage_Title);
	}

	protected void createFieldEditors() {
		setDescription(Messages.RemoteSimulatorPreferencePage_Description);

		Composite fieldEditorParent = getFieldEditorParent();

		// Field for local simulator install directory
		this.simulatorBaseDirectory = new StringFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY,
				Messages.RemoteSimulatorPreferencePage_LabelSimulatorBaseDirectory, fieldEditorParent);
		this.simulatorBaseDirectory.setEmptyStringAllowed(false);
		addField(this.simulatorBaseDirectory);

		// Field for local simulator working directory
		this.workDirectory = new StringFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_WORK_DIRECTORY,
				Messages.RemoteSimulatorPreferencePage_LabelWorkDirectory, fieldEditorParent);
		this.workDirectory.setEmptyStringAllowed(false);
		addField(this.workDirectory);

		// Field for the local simulator Linux kernel image
		this.kernelImagePath = new StringFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_KERNEL_IMAGE_PATH,
				Messages.RemoteSimulatorPreferencePage_LabelKernelImagePath, fieldEditorParent);
		this.kernelImagePath.setEmptyStringAllowed(false);
		addField(this.kernelImagePath);

		// Field for the local simulator Linux root file system image
		this.rootImagePath = new StringFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_ROOT_IMAGE_PATH,
				Messages.RemoteSimulatorPreferencePage_LabelRootImagePath, fieldEditorParent);
		this.rootImagePath.setEmptyStringAllowed(false);
		addField(this.rootImagePath);

		addSpace(fieldEditorParent);

		// Check box to control if the Linux console will be shown or not
		BooleanFieldEditor showLinuxConsole = new BooleanFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_CONSOLE_SHOW_LINUX,
				Messages.RemoteSimulatorPreferencePage_LabelShowLinuxConsole, fieldEditorParent);
		addField(showLinuxConsole);

		// Check box to control if the simulator console will be shown or not
		BooleanFieldEditor showSimulatorConsole = new BooleanFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR,
				Messages.RemoteSimulatorPreferencePage_LabelShowSimulatorConsole, fieldEditorParent);
		addField(showSimulatorConsole);

		// Check box to control if the Mambo GUI will be shown or not
		BooleanFieldEditor showMamboGUI = new BooleanFieldEditor(
				RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_SHOW_SIMULATOR_GUI,
				Messages.RemoteSimulatorPreferencePage_LabelShowSimulatorGUI, fieldEditorParent);
		addField(showMamboGUI);
		
		addSpace(fieldEditorParent);
		addField(new LabelFieldEditor(Messages.RemoteSimulatorPreferencePage_HeaderLaunch, getFieldEditorParent()));
		StringFieldEditor sysWorkspaceDir = new StringFieldEditor(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX +
				RemoteConfigurationBean.ATTR_SYSTEM_WORKSPACE,
				Messages.RemoteSimulatorPreferencePage_LabelSystemWorkspace, fieldEditorParent);
		addField(sysWorkspaceDir);

	}

	/**
	 * Initializes all fields editors, searchers and property listeners.
	 */
	protected void initialize() {
		super.initialize();
	}

	protected void addSpace(Composite fieldEditorParent) {
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(fieldEditorParent);
		addField(spacer1);
	}

	public void init(IWorkbench workbench) {
	}

}
