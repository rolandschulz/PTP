/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
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
import org.eclipse.cdt.managedbuilder.core.IToolChain;
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
public class SynchronizeWizardExtensionPage extends WizardPage {
	private static final String ConfigMapKey = "config-map"; //$NON-NLS-1$
	private static final String ToolchainMapKey = "toolchain-map"; //$NON-NLS-1$
	private static final String SyncConfigSetKey = "sync-config-set"; //$NON-NLS-1$
	private static final String ProjectNameKey = "project-name"; //$NON-NLS-1$
	public static final String CDT_CONFIG_PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.CConfigWizardPage"; //$NON-NLS-1$

	private String fConfigName;
	
	private Composite parentComposite;
	private Composite composite;
	private Composite defaultBuildConfigWidget;
	
	private Map<String, String> syncConfigToBuildConfigMap = new HashMap<String, String>();
	private Map<String, String> syncConfigToToolchainMap;
	private Map<String, String> toolchainToBuildConfigMap = new HashMap<String, String>();

	public SynchronizeWizardExtensionPage() {
		super("CDT SyncConfigToBuildConfigWizardPage"); //$NON-NLS-1$
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
		defaultBuildConfigWidget = new DefaultBuildConfigWidget(parent, SWT.NONE, getSyncConfigs(), getBuildConfigs());
		
	}

	private IProject getAndValidateProject() {
		String projectName = SyncWizardDataCache.getProperty(ProjectNameKey);
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
			IToolChain toolchain = h.getToolChain();
			String toolchainName;
			if (toolchain == null) {
				toolchainName = "No ToolChain"; //$NON-NLS-1$
			} else {
				toolchainName = h.getToolChain().getName();
			}
			if (toolchainName != null) {
				toolchainToBuildConfigMap.put(toolchainName, h.getName());
			}
		}
		return configNames;
	}

	private String[] getBuildConfigs() {
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
		}
		return buildConfigNames.toArray(new String[0]);
	}

	private String[] getSyncConfigs() {
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

    /**
     * Page rendering depends on state set in other pages, so we need to render the page just before it becomes visible:
     * http://stackoverflow.com/questions/10303123/how-to-catch-first-time-displaying-of-the-wizardpage
     */
	@Override
	public void setVisible(boolean isVisible) {
		update();
		super.setVisible(isVisible);
	}
}