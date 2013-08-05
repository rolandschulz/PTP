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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.ems.ui.EnvManagerConfigWidget;
import org.eclipse.ptp.ems.ui.IErrorListener;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.SyncConfigListenerCDT;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.remotemake.SyncCommandLauncher;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.AddSyncConfigWizardPage;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
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
 * Synchronize Properties page extension for CDT to specify default build configurations
 */
public class SynchronizeProperties extends AbstractSynchronizeProperties {
	private Group fUserDefinedContent;
	private Combo fConfigCombo;
	private SyncConfig fSyncConfig;
	private IEnvManagerConfig fEnvConfig;
	private AddSyncConfigWizardPage fWizardPage;
	private EnvManagerConfigWidget fEnvWidget;

	private final Map<String, String> fBuildConfigNameToIdMap = new HashMap<String, String>();
	private final Map<SyncConfig, String> fDirtyBuildConfigs = new HashMap<SyncConfig, String>();
	private final Map<SyncConfig, IEnvManagerConfig> fDirtyEnvConfigs = new HashMap<SyncConfig, IEnvManagerConfig>();

	public SynchronizeProperties(ISynchronizePropertiesDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public void addConfiguration(SyncConfig config) {
		String buildConfigName = fWizardPage.getBuildConfiguration();
		if (buildConfigName != null) {
			fDirtyBuildConfigs.put(config, fWizardPage.getBuildConfiguration());
		}
	}

	/**
	 * Cache the config if it has changed or remove it from the cache if it has not.
	 */
	private void cacheConfig() {
		String selectedConfigName = null;
		int index = fConfigCombo.getSelectionIndex();
		if (index >= 0) {
			selectedConfigName = fConfigCombo.getItem(index);
		}

		String defaultConfigName = getDefaultBuildConfig(fSyncConfig);
		if (defaultConfigName == null || !defaultConfigName.equals(selectedConfigName)) {
			fDirtyBuildConfigs.put(fSyncConfig, selectedConfigName);
		} else {
			fDirtyBuildConfigs.remove(fSyncConfig);
		}

		fEnvWidget.saveConfiguration(fEnvConfig);
		fDirtyEnvConfigs.put(fSyncConfig, fEnvConfig);
	}

	private List<String> computeSelectedItems() {
		try {
			final IEnvManagerConfig config = getEnvConfig();
			if (config.getConnectionName().equals(fEnvWidget.getConnectionName())) {
				return config.getConfigElements();
			} else {
				// If the stored connection name is different,
				// then the stored list of modules is probably for a different machine,
				// so don't try to select those modules, since they're probably incomplete or invalid for this connection
				return null; // Revert to default selection
			}
		} catch (final Error e) {
			return null; // Revert to default selection
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
		fUserDefinedContent.setText(Messages.SynchronizeProperties_Title);
		fUserDefinedContent.setLayout(new GridLayout(2, false));
		fUserDefinedContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Label label = new Label(fUserDefinedContent, SWT.NONE);
		label.setText(Messages.SynchronizeProperties_Default_configuration);
		fConfigCombo = new Combo(fUserDefinedContent, SWT.READ_ONLY);
		readBuildConfigData(config.getProject());
		fConfigCombo.setItems(fBuildConfigNameToIdMap.keySet().toArray(new String[0]));
		fConfigCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		fEnvWidget = new EnvManagerConfigWidget(fUserDefinedContent, SWT.NONE);
		fEnvWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		fEnvWidget.setErrorListener(new IErrorListener() {
			@Override
			public void errorCleared() {
				// setErrorMessage(null); TODO: work out what to do here
			}

			@Override
			public void errorRaised(String message) {
				// setErrorMessage(message); TODO: work out what to do here
			}
		});

		String initialBuildConfig;
		if (fDirtyBuildConfigs.containsKey(config)) {
			initialBuildConfig = fDirtyBuildConfigs.get(config);
		} else {
			initialBuildConfig = getDefaultBuildConfig(config);
		}
		selectBuildConfiguration(initialBuildConfig);
		if (fDirtyEnvConfigs.containsKey(config)) {
			fEnvConfig = fDirtyEnvConfigs.get(config);
		}
		setEnvConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#disposePropertiesConfigurationArea()
	 */
	@Override
	public void disposePropertiesConfigurationArea() {
		if (fUserDefinedContent != null) {
			if (!fUserDefinedContent.isDisposed()) {
				cacheConfig();
				fUserDefinedContent.dispose();
			}
			fUserDefinedContent = null;
			fEnvConfig = null;
		}
	}

	/**
	 * Get name of the default build config for the given sync config
	 * 
	 * @param syncConfig
	 * @return build config name or null if either the sync config does not have a default build config or if the build config no
	 *         longer
	 *         exists in CDT.
	 */
	private String getDefaultBuildConfig(SyncConfig syncConfig) {
		String buildConfigId = syncConfig.getProperty(SyncConfigListenerCDT.DEFAULT_BUILD_CONFIG_ID);
		if (buildConfigId != null) {
			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(syncConfig.getProject());
			IConfiguration buildConfig = buildInfo.getManagedProject().getConfiguration(buildConfigId);
			if (buildConfig != null) {
				return buildConfig.getName();
			}
		}
		return null;
	}

	private IEnvManagerConfig getEnvConfig() {
		try {
			if (fEnvConfig == null) {
				fEnvConfig = new EnvManagerConfigString(fSyncConfig.getProperty(SyncCommandLauncher.EMS_CONFIG_PROPERTY));
			}
			return fEnvConfig;
		} catch (final Error e) {
			// TODO: work out what to do here
			//			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			Activator.log(e);
			throw e;
		}
	}

	private String getManualConfigText() {
		try {
			return getEnvConfig().getManualConfigText();
		} catch (final Error e) {
			return ""; //$NON-NLS-1$
		}
	}

	private URI getSyncURI() {
		try {
			return SyncConfigManager.getActiveSyncLocationURI(fSyncConfig.getProject());
		} catch (CoreException e) {
			// TODO: work out what to do here
			//			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			Activator.log(e);
		}
		return null;
	}

	private boolean isEnvConfigSupportEnabled() {
		try {
			return getEnvConfig().isEnvMgmtEnabled();
		} catch (final Error e) {
			return false;
		}
	}

	private boolean isManualConfigEnabled() {
		try {
			return getEnvConfig().isManualConfigEnabled();
		} catch (final Error e) {
			return false;
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
		if ((fUserDefinedContent != null) && !fUserDefinedContent.isDisposed()) {
			cacheConfig();
		}

		Set<IProject> projectsToUpdate = new HashSet<IProject>();
		/*
		 * Iterate through all the potentially changed configurations and update the build configuration information
		 */
		for (Entry<SyncConfig, String> dirty : fDirtyBuildConfigs.entrySet()) {
			// Must store build config Id - not name
			String configId = fBuildConfigNameToIdMap.get(dirty.getValue());
			dirty.getKey().setProperty(SyncConfigListenerCDT.DEFAULT_BUILD_CONFIG_ID, configId);
			projectsToUpdate.add(dirty.getKey().getProject());
		}

		for (Entry<SyncConfig, IEnvManagerConfig> dirty : fDirtyEnvConfigs.entrySet()) {
			dirty.getKey().setProperty(SyncCommandLauncher.EMS_CONFIG_PROPERTY, dirty.getValue().toString());
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

		fDirtyBuildConfigs.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performCancel()
	 */
	@Override
	public void performCancel() {
		fDirtyBuildConfigs.clear();
		fDirtyEnvConfigs.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties#performDefaults()
	 */
	@Override
	public void performDefaults() {
		if (fUserDefinedContent != null) {
			String buildConfigName = getDefaultBuildConfig(fSyncConfig);
			selectBuildConfiguration(buildConfigName);
			fEnvConfig = null;
			setEnvConfig();
		}
		fDirtyBuildConfigs.clear();
		fDirtyEnvConfigs.clear();
	}

	/**
	 * Read build configuration data and populate data structures
	 * 
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
	 * 
	 * @param configName
	 *            build config name or null to select none
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

	private void setEnvConfig() {
		try {
			fEnvWidget.setConnection(fSyncConfig.getRemoteConnection());
			fEnvWidget.setUseEMSCheckbox(isEnvConfigSupportEnabled());
			fEnvWidget.setManualConfigCheckbox(isManualConfigEnabled());
			fEnvWidget.setManualConfigText(getManualConfigText());
			fEnvWidget.configurationChanged(getSyncURI(), fSyncConfig.getRemoteConnection(), computeSelectedItems());
		} catch (MissingConnectionException e) {
			Activator.log(e);
		}
	}
}
