/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.wizards;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage;
import org.eclipse.ptp.rm.ui.wizards.AbstractToolRMConfigurationWizardPage;
import org.eclipse.ptp.rm.ui.wizards.WizardPageDataSource;
import org.eclipse.ptp.rm.ui.wizards.WizardPageWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class OpenMPIConfigurationWizardPage extends AbstractToolRMConfigurationWizardPage {
	/**
	 * @since 2.0
	 */
	protected final String versionIds[] = new String[] { IOpenMPIResourceManagerConfiguration.VERSION_AUTO,
			IOpenMPIResourceManagerConfiguration.VERSION_12, IOpenMPIResourceManagerConfiguration.VERSION_13,
			IOpenMPIResourceManagerConfiguration.VERSION_14 };
	private final String versionsNames[] = new String[] { Messages.OpenMPIConfigurationWizardPage_VersionCombo_Auto,
			Messages.OpenMPIConfigurationWizardPage_VersionCombo_Version12,
			Messages.OpenMPIConfigurationWizardPage_VersionCombo_Version13,
			Messages.OpenMPIConfigurationWizardPage_VersionCombo_Version14 };

	protected Combo versionCombo;

	protected class WidgetListener extends AbstractToolRMConfigurationWizardPage.WidgetListener {
		@Override
		protected void doModifyText(ModifyEvent evt) {
			super.doModifyText(evt);
		}

		@Override
		public void doWidgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == versionCombo) {
				handleVersionSelected();
				updateControls();
				getDataSource().storeAndValidate();
			} else {
				super.doWidgetSelected(e);
			}
		}
	}

	protected class DataSource extends AbstractToolRMConfigurationWizardPage.DataSource {
		protected DataSource(AbstractConfigurationWizardPage page) {
			super(page);
		}

		private IOpenMPIResourceManagerConfiguration config;
		private String versionId = null;

		public String getVersionId() {
			return versionId;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			if (versionCombo.getSelectionIndex() == -1) {
				versionId = null;
			} else {
				versionId = versionIds[versionCombo.getSelectionIndex()];
			}
			super.copyFromFields();
		}

		@Override
		protected void copyToFields() {
			if (versionId == null) {
				versionCombo.select(-1);
			} else {
				for (int i = 0; i < versionIds.length; i++) {
					if (versionId.equals(versionIds[i])) {
						versionCombo.select(i);
					}
				}
			}
			super.copyToFields();
		}

		@Override
		protected void loadFromStorage() {
			versionId = config.getVersionId();
			super.loadFromStorage();
		}

		@Override
		protected void copyToStorage() {
			config.setVersionId(versionId);
			super.copyToStorage();
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (versionId == null) {
				throw new ValidationException(Messages.OpenMPIConfigurationWizardPage_Validation_NoVersionSelected);
			}
			super.validateLocal();
		}

		@Override
		public void setConfiguration(IResourceManagerComponentConfiguration configuration) {
			this.config = (IOpenMPIResourceManagerConfiguration) configuration;
			super.setConfiguration(configuration);
		}
	}

	@Override
	protected WizardPageWidgetListener createListener() {
		return new WidgetListener();
	}

	@Override
	protected WizardPageDataSource createDataSource() {
		return new DataSource(this);
	}

	public OpenMPIConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, IOpenMPIResourceManagerConfiguration.OPENMPI_CAPABILITIES, Messages.OpenMPIConfigurationWizardPage_Name,
				Messages.OpenMPIConfigurationWizardPage_Title, Messages.OpenMPIConfigurationWizardPage_Description);
	}

	@Override
	protected Composite doCreateContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginLeft = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		contents.setLayout(layout);

		createVersionContents(contents);
		createContents(contents);

		return contents;
	}

	protected void createVersionContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		contents.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		contents.setLayoutData(gd);

		/*
		 * Selection for openmpi version.
		 */

		Label label = new Label(contents, SWT.NONE);
		label.setText(Messages.OpenMPIConfigurationWizardPage_Label_Version);

		versionCombo = new Combo(contents, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (int i = 0; i < versionsNames.length; i++) {
			versionCombo.add(versionsNames[i]);
		}
		versionCombo.addSelectionListener(getWidgetListener());
	}

	public void handleVersionSelected() {
		getWidgetListener().disable();
		DataSource dataSource = (DataSource) getDataSource();
		dataSource.justValidate();
		resetErrorMessages();
		setToolCommandDefaults();
		setInstallPathDefaults();
		dataSource.copyToFields();
		getWidgetListener().enable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractToolRMConfigurationWizardPage#
	 * setToolCommandDefaults()
	 */
	@Override
	protected void setToolCommandDefaults() {
		DataSource dataSource = (DataSource) getDataSource();
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		boolean enabled = true;
		if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
			launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
					+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
					+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
					+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			enabled = false;
		} else if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
			launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
					+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
					+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
					+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
		} else if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_13)) {
			launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
					+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
					+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
					+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
		} else if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_14)) {
			launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
					+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
					+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
					+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
		} else {
			assert false;
		}
		dataSource.setCommands(launchCmd, debugCmd, discoverCmd, null, 0, null);
		dataSource.setCommandsEnabled(enabled);
		dataSource.setUseDefaults(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractToolRMConfigurationWizardPage#
	 * setInstallPathDefaults()
	 */
	@Override
	protected void setInstallPathDefaults() {
		DataSource dataSource = (DataSource) getDataSource();
		String remoteInstallPath = null;
		if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
			remoteInstallPath = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
					+ OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
			remoteInstallPath = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
					+ OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_13)) {
			remoteInstallPath = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
					+ OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else if (dataSource.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_14)) {
			remoteInstallPath = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
					+ OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else {
			assert false;
		}
		dataSource.setInstallPath(remoteInstallPath);
		dataSource.setInstallDefaults(true);
	}
}
