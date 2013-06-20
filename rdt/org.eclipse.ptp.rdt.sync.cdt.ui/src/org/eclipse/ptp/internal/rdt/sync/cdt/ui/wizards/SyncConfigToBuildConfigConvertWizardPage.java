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
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Page appended to the synchronize project's conversion wizard for selecting default build configurations.
 * This class and {@link #SyncConfigToBuildConfigNewWizardPage} are similar.
 */
public class SyncConfigToBuildConfigConvertWizardPage extends WizardPage {
	private static final String SyncConfigSetKey = "sync-config-set"; //$NON-NLS-1$
	private static final String ProjectNameKey = "project-name"; //$NON-NLS-1$
	public static final String CDT_CONFIG_PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.CConfigWizardPage"; //$NON-NLS-1$

	private String[] syncConfigNames;
	private String[] buildConfigNames;

	private Composite parentComposite = null;
	private DefaultBuildConfigWidget configWidget = null;

	public SyncConfigToBuildConfigConvertWizardPage() {
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
		update();		
	}

	private IProject getAndValidateProject() {
		String projectName = SyncWizardDataCache.getProperty(ProjectNameKey);
		if (projectName == null) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assert project != null : Messages.SyncConfigToBuildConfigWizardPage_4 + projectName;
		assert isCDTProject(project) : Messages.SyncConfigToBuildConfigWizardPage_5 + project.getName();
		return project;
	}

	private void getCachedData() {
		IProject project = this.getAndValidateProject();
		getBuildConfigData(project);
		getSyncConfigData();
	}

	private void getBuildConfigData(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] buildConfigs = buildInfo.getManagedProject().getConfigurations();		
		ArrayList<String> configNames = new ArrayList<String>();
		for (IConfiguration config : buildConfigs) {
			configNames.add(config.getName());
		}
		buildConfigNames = new String[configNames.size()];
		configNames.toArray(buildConfigNames);
	}

	private void getSyncConfigData() {
		Set<String> configNames = SyncWizardDataCache.getMultiValueProperty(SyncConfigSetKey);
		assert configNames != null && configNames.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_2;
		syncConfigNames = new String[configNames.size()];
		configNames.toArray(syncConfigNames);
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
     * Page rendering depends on state set in other pages, so we need to render the page just before it becomes visible:
     * http://stackoverflow.com/questions/10303123/how-to-catch-first-time-displaying-of-the-wizardpage
     */
	@Override
	public void setVisible(boolean isVisible) {
		if (isVisible) {
			update();
		}
		super.setVisible(isVisible);
	}
	
	private void update() {
		getCachedData();
		if (configWidget != null) {
			configWidget.dispose();
		}
		configWidget = new DefaultBuildConfigWidget(parentComposite, SWT.NONE, syncConfigNames, buildConfigNames,
				new HashMap<String, String>());
		setControl(configWidget);
		parentComposite.layout(true, true);
	}
}