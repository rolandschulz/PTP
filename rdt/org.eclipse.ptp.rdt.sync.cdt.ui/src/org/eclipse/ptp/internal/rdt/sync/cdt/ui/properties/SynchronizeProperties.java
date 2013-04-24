/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.BuildConfigUtils;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.AddSyncConfigWizardPage;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.ui.AbstractSynchronizeProperties;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class SynchronizeProperties extends AbstractSynchronizeProperties {
	private Group fUserDefinedContent;
	private Combo fConfigCombo;
	private SyncConfig fSyncConfig;
	private AddSyncConfigWizardPage fWizardPage;
	private final Map<SyncConfig, String> fDirtySyncConfigs = new HashMap<SyncConfig, String>();

	public SynchronizeProperties(ISynchronizePropertiesDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public void addConfiguration(SyncConfig config) {
		fDirtySyncConfigs.put(config, fWizardPage.getBuildConfiguration());
	}

	/**
	 * Check if the selected build configuration has changed. Save the corresponding sync configuration if it has.
	 */
	private void checkConfig() {
		IConfiguration config = BuildConfigUtils
				.getBuildConfigurationForSyncConfig(fSyncConfig.getProject(), fSyncConfig.getName());
		if (config != null) {
			int index = fConfigCombo.getSelectionIndex();
			if (index >= 0) {
				String configName = fConfigCombo.getItem(index);
				if (!config.getName().equals(configName)) {
					fDirtySyncConfigs.put(fSyncConfig, configName);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#createAddWizardPages(org.eclipse.core.resources.IProject)
	 */
	@Override
	public WizardPage[] createAddWizardPages(IProject project) {
		fWizardPage = new AddSyncConfigWizardPage(project);
		return new WizardPage[] { fWizardPage };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#createPropertiesConfigurationArea(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.core.resources.IProject, org.eclipse.jface.operation.IRunnableContext)
	 */
	@Override
	public void createPropertiesConfigurationArea(Composite parent, SyncConfig config) {
		fSyncConfig = config;
		fUserDefinedContent = new Group(parent, SWT.NONE);
		fUserDefinedContent.setText("CDT Build Configurations"); //$NON-NLS-1$
		fUserDefinedContent.setLayout(new GridLayout(2, false));
		fUserDefinedContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Label label = new Label(fUserDefinedContent, SWT.NONE);
		label.setText(Messages.SynchronizeProperties_Link_configuration); 
		fConfigCombo = new Combo(fUserDefinedContent, SWT.READ_ONLY);
		fConfigCombo.setItems(getConfigurationNames(config.getProject()));
		fConfigCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		selectConfiguration(fConfigCombo, config.getProject(), config.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#disposePropertiesConfigurationArea()
	 */
	@Override
	public void disposePropertiesConfigurationArea() {
		if (fUserDefinedContent != null) {
			checkConfig();
			fUserDefinedContent.dispose();
			fUserDefinedContent = null;
		}
	}

	private String[] getConfigurationNames(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo != null) {
			return buildInfo.getConfigurationNames();
		}
		return new String[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performApply()
	 */
	@Override
	public void performApply() {
		/*
		 * Iterate through all the potentially changed configurations and update the build configuration information
		 */
		for (Entry<SyncConfig, String> dirty : fDirtySyncConfigs.entrySet()) {
			IProject project = dirty.getKey().getProject();
			String syncConfigName = dirty.getKey().getName();
			String buildConfigName = dirty.getValue();
			// BuildConfigUtils.updateProject(project, syncConfigName, buildConfigName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performCancel()
	 */
	@Override
	public void performCancel() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performDefaults()
	 */
	@Override
	public void performDefaults() {
		if (fUserDefinedContent != null) {
			selectConfiguration(fConfigCombo, fSyncConfig.getProject(), fSyncConfig.getName());
		}
	}

	private void selectConfiguration(Combo combo, IProject project, String name) {
		IConfiguration config = BuildConfigUtils.getBuildConfigurationForSyncConfig(project, name);
		if (config != null) {
			for (int i = 0; i < combo.getItemCount(); i++) {
				if (combo.getItem(i).equals(config.getName())) {
					combo.select(i);
					return;
				}
			}
		}
	}
}