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

import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpi12PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpi13PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMpiResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.wizards.AbstractToolRMConfigurationWizardPage;
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

public class OpenMpiToolConfigurationWizardPage extends
		AbstractToolRMConfigurationWizardPage {

	String versionIds[] = new String[] { OpenMpiResourceManagerConfiguration.VERSION_12, OpenMpiResourceManagerConfiguration.VERSION_13 };
	String versionsNames[] = new String[] { "openmpi 1.2", "openmpi 1.3"};

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
			} else {
				super.doWidgetSelected(e);
			}
		}
	}

	protected class DataSource extends AbstractToolRMConfigurationWizardPage.DataSource {
		private OpenMpiResourceManagerConfiguration config;
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
		protected void loadConfig() {
			versionId = config.getVersionId();
			super.loadConfig();
		}

		@Override
		protected void storeConfig() {
			config.setVersionId(versionId);
			super.storeConfig();
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (versionId == null) {
				throw new ValidationException("No openmpi version selected");
			}
			super.validateLocal();
		}


		@Override
		public void setConfig(IResourceManagerConfiguration configuration) {
			this.config = (OpenMpiResourceManagerConfiguration) configuration;
			super.setConfig(configuration);
		}
	}

	@Override
	protected WizardListener createListener() {
		return new WidgetListener();
	}

	@Override
	protected DataSource createDataSource() {
		return new DataSource();
	}

	public OpenMpiToolConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, OpenMpiResourceManagerConfiguration.OPENMPI_CAPABILITIES , "Open MPI", "Open MPI tool configuration", "Enter information to configure the Open MPI tool");
	}

	@Override
	protected void createContents(Composite parent) {
		createVersionContents(parent);
		createOpenMpiContests(parent);
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
		label.setText("openmpi version:");

		versionCombo = new Combo(contents, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (int i = 0; i < versionsNames.length; i++) {
			versionCombo.add(versionsNames[i]);
		}
		versionCombo.addSelectionListener(listener);
	}

	public void handleVersionSelected() {
		listenerEnabled = false;
		DataSource dataSource = (DataSource) this.dataSource;
		dataSource.getFromFields();
		String launchCmd = null;
		String discoverCmd = null;
		if (dataSource.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_12)) {
			Preferences preferences = OpenMpi12PreferenceManager.getPreferences();
			launchCmd = preferences.getString(OpenMpi12PreferenceManager.PREFIX + OpenMpi12PreferenceManager.PREFS_LAUNCH_CMD);
			discoverCmd = preferences.getString(OpenMpi12PreferenceManager.PREFIX + OpenMpi12PreferenceManager.PREFS_DISCOVER_CMD);
		} else if (dataSource.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_13)) {
			Preferences preferences = OpenMpi13PreferenceManager.getPreferences();
			launchCmd = preferences.getString(OpenMpi13PreferenceManager.PREFIX + OpenMpi13PreferenceManager.PREFS_LAUNCH_CMD);
			discoverCmd = preferences.getString(OpenMpi13PreferenceManager.PREFIX + OpenMpi13PreferenceManager.PREFS_DISCOVER_CMD);
		} else {
			assert false;
		}
		dataSource.setCommandFields(launchCmd, discoverCmd, null, 0, null, dataSource.getRemoteInstallPath());
		dataSource.putToFields();
		listenerEnabled = true;
	}


}
