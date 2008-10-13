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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI12PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI13PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage;
import org.eclipse.ptp.rm.ui.wizards.AbstractToolRMConfigurationWizardPage;
import org.eclipse.ptp.rm.ui.wizards.WizardPageDataSource;
import org.eclipse.ptp.rm.ui.wizards.WizardPageWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
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
public class OpenMPIConfigurationWizardPage extends
AbstractToolRMConfigurationWizardPage {

	String versionIds[] = new String[] { OpenMPIResourceManagerConfiguration.VERSION_12, OpenMPIResourceManagerConfiguration.VERSION_13 };
	String versionsNames[] = new String[] { Messages.OpenMPIConfigurationWizardPage_VersionCombo_Version12, Messages.OpenMPIConfigurationWizardPage_VersionCombo_Version13};

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

		private OpenMPIResourceManagerConfiguration config;
		private String versionId = null;

		public String getVersionId() {
			return versionId;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			if (versionCombo.getSelectionIndex() == -1) {
				versionIds = null;
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
			if (versionId == null)
				throw new ValidationException(Messages.OpenMPIConfigurationWizardPage_Validation_NoVersionSelected);
			super.validateLocal();
		}


		@Override
		public void setConfig(IResourceManagerConfiguration configuration) {
			this.config = (OpenMPIResourceManagerConfiguration) configuration;
			super.setConfig(configuration);
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

	public OpenMPIConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, OpenMPIResourceManagerConfiguration.OPENMPI_CAPABILITIES , Messages.OpenMPIConfigurationWizardPage_Name, Messages.OpenMPIConfigurationWizardPage_Title, Messages.OpenMPIConfigurationWizardPage_Description);
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
		createOpenMPIContents(contents);

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
		DataSource dataSource = (DataSource) this.getDataSource();
		dataSource.justValidate();
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String remoteInstallPath = null;
		if (dataSource.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
			Preferences preferences = OpenMPI12PreferenceManager.getPreferences();
			launchCmd = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_DISCOVER_CMD);
			remoteInstallPath = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else if (dataSource.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
			Preferences preferences = OpenMPI13PreferenceManager.getPreferences();
			launchCmd = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_DISCOVER_CMD);
			remoteInstallPath = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else {
			assert false;
		}
		resetErrorMessages();
		dataSource.setCommandFields(launchCmd, debugCmd, discoverCmd, null, 0, null, dataSource.getRemoteInstallPath());
		dataSource.setUseDefaults(true);
		dataSource.copyToFields();
		getWidgetListener().enable();
	}
}
