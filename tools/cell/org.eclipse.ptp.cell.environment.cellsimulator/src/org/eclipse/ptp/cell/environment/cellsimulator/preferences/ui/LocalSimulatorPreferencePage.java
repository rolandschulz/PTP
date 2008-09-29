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
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.SimulatorProperties;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalConfigurationBean;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalLaunchAutomaticAttributeGenerator;
import org.eclipse.ptp.cell.environment.cellsimulator.preferences.core.InstallDirectorySearcher;
import org.eclipse.ptp.cell.preferences.core.StringFieldEditorPreferenceSearcher;
import org.eclipse.ptp.cell.preferences.events.FollowBeginStringFieldEditorPropertyChangeListener;
import org.eclipse.ptp.cell.preferences.ui.DirectoryFieldEditorWithSearch;
import org.eclipse.ptp.cell.preferences.ui.LabelFieldEditor;
import org.eclipse.ptp.cell.preferences.ui.SpacerFieldEditor;
import org.eclipse.ptp.cell.utils.packagemanager.PackageManagementSystemManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.2.1
 */
public class LocalSimulatorPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	private DirectoryFieldEditorWithSearch simulatorBaseDirectory;

	private DirectoryFieldEditor workDirectory;

	private FileFieldEditor kernelImagePath;

	private FileFieldEditor rootImagePath;

	private BooleanFieldEditor showSimulatorConsole;

	private BooleanFieldEditor showLinuxConsole;

	private BooleanFieldEditor showMamboGUI;

	private boolean isSimulatorInstalled = false;

	public LocalSimulatorPreferencePage() {
		super(Messages.LocalSimulatorPreferencePage_Title, null, GRID);

		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);

		// Check if simulator is installed.
		if (!PackageManagementSystemManager.getPackageManager().query(SimulatorProperties.simulatorPackage)) {
			this.isSimulatorInstalled = true;
			setDescription(Messages.LocalSimulatorPreferencePage_Description_NoLocalSimulator);
		} else {
			setDescription(Messages.LocalSimulatorPreferencePage_Description);
		}
	}

	protected void createFieldEditors() {
		setTitle(Messages.LocalSimulatorPreferencePage_Title);

		if (this.isSimulatorInstalled) {
			return;
		}

		// Field for local simulator install directory
		this.simulatorBaseDirectory = new DirectoryFieldEditorWithSearch(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY,
				Messages.LocalSimulatorPreferencePage_LabelSimulatorBaseDirectory,
				getFieldEditorParent());
		this.simulatorBaseDirectory
				.setSearchButtonText(Messages.searchButtonText);
		this.simulatorBaseDirectory.setEmptyStringAllowed(false);
		addField(this.simulatorBaseDirectory);

		// Field for local simulator working directory
		this.workDirectory = new DirectoryFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_WORK_DIRECTORY,
				Messages.LocalSimulatorPreferencePage_LabelWorkDirectory,
				getFieldEditorParent());
		this.workDirectory.setEmptyStringAllowed(false);
		addField(this.workDirectory);

		// Field for the local simulator Linux kernel image
		this.kernelImagePath = new FileFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_KERNEL_IMAGE_PATH,
				Messages.LocalSimulatorPreferencePage_LabelKernelImagePath,
				getFieldEditorParent());
		this.kernelImagePath.setEmptyStringAllowed(false);
		addField(this.kernelImagePath);

		// Field for the local simulator Linux root file system image
		this.rootImagePath = new FileFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_ROOT_IMAGE_PATH,
				Messages.LocalSimulatorPreferencePage_LabelRootImagePath,
				getFieldEditorParent());
		this.rootImagePath.setEmptyStringAllowed(false);
		addField(this.rootImagePath);

		addSpace(getFieldEditorParent());

		showLinuxConsole = new BooleanFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_CONSOLE_SHOW_LINUX,
				Messages.LocalSimulatorPreferencePage_LabelShowLinuxconsole,
				getFieldEditorParent());
		addField(showLinuxConsole);

		showSimulatorConsole = new BooleanFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR,
				Messages.LocalSimulatorPreferencePage_LabelShowSimulatorConsole,
				getFieldEditorParent());
		addField(showSimulatorConsole);

		showMamboGUI = new BooleanFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_SHOW_SIMULATOR_GUI,
				Messages.LocalSimulatorPreferencePage_LabelShowSimulatorGUI,
				getFieldEditorParent());
		addField(showMamboGUI);

		addSpace(getFieldEditorParent());

		addField(new LabelFieldEditor(
				Messages.LocalSimulatorPreferencePage_NetworkConfigHeader1,
				getFieldEditorParent()));
		addField(new StringFieldEditor(
				LocalLaunchAutomaticAttributeGenerator.ATTR_BASE_NETWORK,
				Messages.LocalSimulatorPreferencePage_LabelBaseNetwork,
				getFieldEditorParent()));

		addField(new StringFieldEditor(
				LocalLaunchAutomaticAttributeGenerator.ATTR_BASE_MACADDRESS,
				Messages.LocalSimulatorPreferencePage_LabelBaseMacaddress,
				getFieldEditorParent()));

		addField(new LabelFieldEditor(
				Messages.LocalSimulatorPreferencePage_PortConfigHeader1,
				getFieldEditorParent()));
		addField(new LabelFieldEditor(
				Messages.LocalSimulatorPreferencePage_PortConfigHeader2,
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				LocalLaunchAutomaticAttributeGenerator.ATTR_MIN_PORTVALUE,
				Messages.LocalSimulatorPreferencePage_LabelMinPort,
				getFieldEditorParent()));

		addField(new IntegerFieldEditor(
				LocalLaunchAutomaticAttributeGenerator.ATTR_MAX_PORTVALUE,
				Messages.LocalSimulatorPreferencePage_LabelMaxPort,
				getFieldEditorParent()));

		addSpace(getFieldEditorParent());
		addField(new LabelFieldEditor(
				Messages.LocalSimulatorPreferencePage_HeaderLaunch,
				getFieldEditorParent()));
		addField(new StringFieldEditor(
				LocalConfigurationBean.ATTR_PREFERENCES_PREFIX
						+ LocalConfigurationBean.ATTR_SYSTEM_WORKSPACE,
				Messages.LocalSimulatorPreferencePage_LabelSystemWorkspace,
				getFieldEditorParent()));

	}

	/**
	 * Initializes all fields editors, searchers and property listeners.
	 */
	protected void initialize() {
		super.initialize();

		if (this.isSimulatorInstalled) {
			return;
		}

		Composite fieldEditorParent = getFieldEditorParent();
		StringFieldEditorPreferenceSearcher installDirectorySearcher = new InstallDirectorySearcher(
				this.simulatorBaseDirectory, fieldEditorParent);
		installDirectorySearcher
				.addPropertyChangeListener(new FollowBeginStringFieldEditorPropertyChangeListener(
						this.workDirectory, fieldEditorParent));
		installDirectorySearcher
				.addPropertyChangeListener(new FollowBeginStringFieldEditorPropertyChangeListener(
						this.kernelImagePath, fieldEditorParent));
		installDirectorySearcher
				.addPropertyChangeListener(new FollowBeginStringFieldEditorPropertyChangeListener(
						this.rootImagePath, fieldEditorParent));
		this.simulatorBaseDirectory.addSearcher(installDirectorySearcher);
	}

	protected void addSpace(Composite fieldEditorParent) {
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(fieldEditorParent);
		addField(spacer1);
	}

	public void init(IWorkbench workbench) {
	}

}
