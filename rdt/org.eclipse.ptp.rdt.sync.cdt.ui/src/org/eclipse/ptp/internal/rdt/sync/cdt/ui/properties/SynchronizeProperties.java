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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.AddSyncConfigWizardPage;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.ui.AbstractSynchronizeProperties;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Synchronize Properties page extension for CDT to specify default build configurations *
 */
public class SynchronizeProperties extends AbstractSynchronizeProperties {
	private static final String DEFAULT_BUILD_CONFIG_ID = "default-build-config-id"; //$NON-NLS-1$

	private Group fUserDefinedContent;
	private Combo fConfigCombo;
	private SyncConfig fSyncConfig;
	private AddSyncConfigWizardPage fWizardPage;
	private final Map<String, String> fBuildConfigNameToIdMap = new HashMap<String, String>();
	private final Map<SyncConfig, String> fDirtySyncConfigs = new HashMap<SyncConfig, String>();

	public SynchronizeProperties(ISynchronizePropertiesDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public void addConfiguration(SyncConfig config) {
		fDirtySyncConfigs.put(config, fWizardPage.getBuildConfiguration());
	}

	/**
	 * Cache the config if it has changed or remove it from the cache if it has not.
	 */
	private void cacheConfig() {
		String defaultConfigName = this.getDefaultBuildConfig(fSyncConfig);

		String selectedConfigName;
		int index = fConfigCombo.getSelectionIndex();
		if (index == -1) {
			selectedConfigName = null;
		} else {
			selectedConfigName = fConfigCombo.getItem(index);
		}
		
		if (defaultConfigName == selectedConfigName) {
			fDirtySyncConfigs.remove(fSyncConfig);
		} else {
			fDirtySyncConfigs.put(fSyncConfig, selectedConfigName);
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
		this.readBuildConfigData(config.getProject());
		fConfigCombo.setItems(fBuildConfigNameToIdMap.keySet().toArray(new String[0]));
		fConfigCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		String initialBuildConfig;
		if (fDirtySyncConfigs.containsKey(config)) {
			initialBuildConfig = fDirtySyncConfigs.get(config);
		} else {
			initialBuildConfig = this.getDefaultBuildConfig(config);
		}
		selectBuildConfiguration(initialBuildConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#disposePropertiesConfigurationArea()
	 */
	@Override
	public void disposePropertiesConfigurationArea() {
		if (fUserDefinedContent == null) {
			return;
		}

		if (!fUserDefinedContent.isDisposed()) {
			cacheConfig();
			fUserDefinedContent.dispose();
		}
		fUserDefinedContent = null;
	}

	/**
	 * Get name of the default build config for the given sync config
	 *
	 * @param syncConfig
	 * @return build config name or null if either the sync config does not have a default build config or if the build config no longer
	 *         exists in CDT.
	 */
	private String getDefaultBuildConfig(SyncConfig syncConfig) {
		String buildConfigId = syncConfig.getProperty(DEFAULT_BUILD_CONFIG_ID);
		if (buildConfigId == null) {
			return null;
		}
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(syncConfig.getProject());
		IConfiguration buildConfig = buildInfo.getManagedProject().getConfiguration(buildConfigId);
		if (buildConfig == null) {
			return null;
		} else {
			return buildConfig.getName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performApply()
	 */
	@Override
	public void performApply() {
		// Don't forget to cache changes to the current config
		cacheConfig();
		Set<IProject> projectsToUpdate = new HashSet<IProject>();
		/*
		 * Iterate through all the potentially changed configurations and update the build configuration information
		 */
		for (Entry<SyncConfig, String> dirty : fDirtySyncConfigs.entrySet()) {
			// Must store build config Id - not name
			String configId = fBuildConfigNameToIdMap.get(dirty.getValue());
			dirty.getKey().setProperty(DEFAULT_BUILD_CONFIG_ID, configId);
			projectsToUpdate.add(dirty.getKey().getProject());
		}

		// This should always only iterate once... right?
		for (IProject p : projectsToUpdate) {
			try {
				SyncConfigManager.saveConfigs(p);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		
		fDirtySyncConfigs.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performCancel()
	 */
	@Override
	public void performCancel() {
		fDirtySyncConfigs.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performDefaults()
	 */
	@Override
	public void performDefaults() {
		if (fUserDefinedContent != null) {
			String buildConfigName = this.getDefaultBuildConfig(fSyncConfig);
			selectBuildConfiguration(buildConfigName);
		}
		fDirtySyncConfigs.clear();
	}

	/**
	 * Read build configuration data and populate data structures
	 * @param project
	 */
	private void readBuildConfigData(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException(Messages.SynchronizeProperties_0 + project.getName());
		}
		IConfiguration[] allBuildConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allBuildConfigs) {
			fBuildConfigNameToIdMap.put(config.getName(), config.getId());
		}
	}

	/**
	 * Select the build config in the combo with the given name or deselect all if null is given or if the name is not found.
	 * @param configName
	 *                build config name or null to select none
	 */
	private void selectBuildConfiguration(String configName) {
		if (configName == null) {
			fConfigCombo.deselectAll();
			return;
		}

		for (int i = 0; i < fConfigCombo.getItemCount(); i++) {
			if (fConfigCombo.getItem(i).equals(configName)) {
				fConfigCombo.select(i);
				return;
			}
		}
		
		fConfigCombo.deselectAll();
	}
}