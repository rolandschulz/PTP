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
package org.eclipse.ptp.rm.mpi.mpich2.ui.wizards;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.IMPICH2ResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.ui.messages.Messages;
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
public class MPICH2ConfigurationWizardPage extends AbstractToolRMConfigurationWizardPage {

	private final String versionsNames[] = new String[] { Messages.MPICH2ConfigurationWizardPage_VersionCombo_Version12,
			Messages.MPICH2ConfigurationWizardPage_VersionCombo_Version13 };

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

		@Override
		protected void copyFromFields() throws ValidationException {
			super.copyFromFields();
		}

		@Override
		protected void copyToFields() {
			super.copyToFields();
		}

		@Override
		protected void loadFromStorage() {
			super.loadFromStorage();
		}

		@Override
		protected void copyToStorage() {
			super.copyToStorage();
		}

		@Override
		protected void validateLocal() throws ValidationException {
			super.validateLocal();
		}

		@Override
		public void setConfiguration(IResourceManagerComponentConfiguration configuration) {
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

	public MPICH2ConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, IMPICH2ResourceManagerConfiguration.MPICH2_CAPABILITIES, Messages.MPICH2ConfigurationWizardPage_Name,
				Messages.MPICH2ConfigurationWizardPage_Title, Messages.MPICH2ConfigurationWizardPage_Description);
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
		label.setText(Messages.MPICH2ConfigurationWizardPage_Label_Version);

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
		DataSource dataSource = (DataSource) this.getDataSource();
		String launchCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_LAUNCH_CMD);
		String debugCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_DEBUG_CMD);
		String discoverCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_DISCOVER_CMD);
		String monitorCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD);
		int monitorTime = Preferences.getInt(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
		dataSource.setCommands(launchCmd, debugCmd, discoverCmd, monitorCmd, monitorTime, null);
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
		String remoteInstallPath = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		dataSource.setInstallPath(remoteInstallPath);
		dataSource.setInstallDefaults(true);
	}
}
