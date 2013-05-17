/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SyncConfigToBuildConfigWizardPage extends WizardPage implements Runnable {
	private static final String ConfigMapKey = "config-map"; //$NON-NLS-1$
	private static final String ToolchainMapKey = "toolchain-map"; //$NON-NLS-1$
	private static final String SyncConfigSetKey = "sync-config-set"; //$NON-NLS-1$
	private static final String ProjectNameKey = "project-name"; //$NON-NLS-1$
	private static final String DEFAULT_BUILD_CONFIG_ID = "default-build-config-id"; //$NON-NLS-1$
	public static final String CDT_CONFIG_PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.CConfigWizardPage"; //$NON-NLS-1$

	private String fConfigName;
	
	private Composite parentComposite;
	private Composite composite;
	
	private Map<String, String> syncConfigToBuildConfigMap = new HashMap<String, String>();
	private Map<String, String> syncConfigToToolchainMap;
	private Map<String, String> toolchainToBuildConfigMap = new HashMap<String, String>();
	private Map<String, String> buildConfigToIdMap = new HashMap<String, String>();

	enum WizardMode {
		NEW, ADD_SYNC, ADD_CDT
	}
	private final WizardMode wizardMode;

	public SyncConfigToBuildConfigWizardPage(WizardMode mode) {
		super("CDT SyncConfigToBuildConfigWizardPage"); //$NON-NLS-1$
		wizardMode = mode;
		setTitle(Messages.SyncConfigToBuildConfigWizardPage_0); 
		setDescription(Messages.SyncConfigToBuildConfigWizardPage_1);
		setPageComplete(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		parentComposite = parent;
		update();
	}

	private IProject getAndValidateProject() {
		String projectName = SyncWizardDataCache.getProperty(getWizard().hashCode(), ProjectNameKey);
		if (projectName == null) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assert project != null : Messages.SyncConfigToBuildConfigWizardPage_4 + projectName;
		return project;
	}
	public String getBuildConfiguration() {
		return fConfigName;
	}

	private CDTConfigWizardPage findCDTConfigPage() {
		for (IWizardPage page = getPreviousPage(); page != null; page = page.getPreviousPage()) {
			if (page instanceof CDTConfigWizardPage) {
				return (CDTConfigWizardPage) page;
			}
		}
		return null;
	}

	private Set<String> getBuildConfigNames() {
		CDTConfigWizardPage configPage = findCDTConfigPage();
		if (configPage == null) {
			Activator.log(Messages.SyncConfigToBuildConfigWizardPage_8);
			return new HashSet<String>();
		}

		Set<String> configNames = new HashSet<String>();
		CfgHolder[] cfgHolders = configPage.getCfgItems(false);
		for (CfgHolder h : cfgHolders) {
			configNames.add(h.getName());
			String toolchainName = h.getToolChain().getName();
			if (toolchainName != null) {
				toolchainToBuildConfigMap.put(toolchainName, h.getName());
			}
		}
		return configNames;
	}

	private String[] getBuildConfigs() {
		switch (wizardMode) {
		case NEW:
		case ADD_CDT:
			Set<String> buildConfigsSet = getBuildConfigNames();
			if (buildConfigsSet == null) {
				return null;
			}
			assert buildConfigsSet.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_7;
			return buildConfigsSet.toArray(new String[0]);
		case ADD_SYNC:
			IProject project = this.getAndValidateProject();
			if (project == null) {
				return null;
			}
			assert isCDTProject(project) : Messages.SyncConfigToBuildConfigWizardPage_5 + project.getName();
			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] buildConfigs = buildInfo.getManagedProject().getConfigurations();		
			ArrayList<String> buildConfigNames = new ArrayList<String>();
			for (IConfiguration config : buildConfigs) {
				buildConfigNames.add(config.getName());
				buildConfigToIdMap.put(config.getName(), config.getId());
			}
			return buildConfigNames.toArray(new String[0]);
		default:
			assert false : Messages.SyncConfigToBuildConfigWizardPage_6;
			return null;
		}
	}

	private String[] getSyncConfigs() {
		switch (wizardMode) {
		case NEW:
		case ADD_SYNC:
			Set<String> syncConfigsSet = SyncWizardDataCache.getMultiValueProperty(getWizard().hashCode(), SyncConfigSetKey);
			if (syncConfigsSet == null) {
				return null;
			}
			assert syncConfigsSet.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_2;
			return syncConfigsSet.toArray(new String[0]);
		case ADD_CDT:
			IProject project = this.getAndValidateProject();
			if (project == null) {
				return null;
			}
			assert isSyncProject(project) : Messages.SyncConfigToBuildConfigWizardPage_5 + project.getName();
			SyncConfig[] syncConfigs = SyncConfigManager.getConfigs(project);
			ArrayList<String> syncConfigNames = new ArrayList<String>();
			for (SyncConfig config : syncConfigs) {
				syncConfigNames.add(config.getName());
			}
			return syncConfigNames.toArray(new String[0]);
		default:
			assert false : Messages.SyncConfigToBuildConfigWizardPage_6;
			return null;
		}
	}

	/**
	 * Test if given project is a CDT project.
	 * @param project
	 * @return whether a CDT project 
	 */
	private boolean isCDTProject(IProject project) {
		try {
			return (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID));
		} catch (CoreException e) {
			Activator.log(e);
			return false;
		}
	}

	/**
	 * Test if given project is a synchronized project
	 * @param project
	 * @return whether a synchronized project
	 */
	private boolean isSyncProject(IProject project) {
		return RemoteSyncNature.hasNature(project);
	}	

	@Override
	public void run() {
		IProject project = this.getAndValidateProject();
		assert project != null : Messages.SyncConfigToBuildConfigWizardPage_9;
		assert isSyncProject(project) && isCDTProject(project) : Messages.SyncConfigToBuildConfigWizardPage_5 + project.getName();

		Map<String, String> configMap = SyncWizardDataCache.getMap(getWizard().hashCode(), ConfigMapKey);
		SyncConfig[] allSyncConfigs = SyncConfigManager.getConfigs(project);
		for (SyncConfig config : allSyncConfigs) {
			String defaultBuildConfig = configMap.get(config.getName());
			if (defaultBuildConfig != null) {
				config.setProperty(DEFAULT_BUILD_CONFIG_ID, defaultBuildConfig);
			}
		}
		
		try {
			SyncConfigManager.saveConfigs(project);
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public void setVisible(boolean isVisible) {
		update();
		super.setVisible(isVisible);
	}

	public void update() {
		if (composite != null) {
			composite.dispose();
		}
		composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Headings
		Label syncConfigLabel = new Label(composite, SWT.CENTER);
		syncConfigLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		syncConfigLabel.setText(Messages.SyncConfigToBuildConfigWizardPage_10);
		Label buildConfigLabel = new Label(composite, SWT.CENTER);
		buildConfigLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		buildConfigLabel.setText(Messages.SyncConfigToBuildConfigWizardPage_11);

		// Get config information from other wizard pages
		String[] syncConfigNames = this.getSyncConfigs();
		String[] buildConfigNames = this.getBuildConfigs();
		syncConfigToToolchainMap = SyncWizardDataCache.getMap(getWizard().hashCode(), ToolchainMapKey);
		if (syncConfigToToolchainMap == null) {
			syncConfigToToolchainMap = new HashMap<String, String>();
		}
		// This will occur when the page is first added to the wizard
		if (syncConfigNames == null || buildConfigNames == null) {
			setControl(composite);
			return;
		}

		for (final String sname : syncConfigNames) {
			// Label for sync config
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
			label.setText(sname + ":"); //$NON-NLS-1$
			final Combo combo = new Combo(composite, SWT.READ_ONLY);

			// Combo for sync config. Contains all build configs with default build config selected.
			
			// Find default build config if sync config has one
			String defaultBuildConfig = "none"; //$NON-NLS-1$
			String defaultToolchain = syncConfigToToolchainMap.get(sname);
			if (defaultToolchain != null && toolchainToBuildConfigMap.containsKey(defaultToolchain)) {
				defaultBuildConfig = toolchainToBuildConfigMap.get(defaultToolchain);
			}
			int toSelect = -1;
			for (String bname : buildConfigNames) {
				if (defaultBuildConfig.equals(bname)) {
					toSelect = combo.getItemCount();
				}
				combo.add(bname);
			}
			if (toSelect > -1) {
				combo.select(toSelect);
			}

			combo.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = combo.getSelectionIndex();
					if (index >= 0) {
						String buildConfigId = buildConfigToIdMap.get(combo.getText());
						syncConfigToBuildConfigMap.put(sname, buildConfigId);
					} else {
						syncConfigToBuildConfigMap.remove(sname);
					}
					SyncWizardDataCache.setMap(getWizard().hashCode(), ConfigMapKey, syncConfigToBuildConfigMap);
				}
			});
		}
		setControl(composite);
		parentComposite.layout(true, true);
	}
}