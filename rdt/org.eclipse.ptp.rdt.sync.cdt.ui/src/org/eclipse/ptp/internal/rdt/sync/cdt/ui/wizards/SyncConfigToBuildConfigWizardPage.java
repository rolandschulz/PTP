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
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
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
public class SyncConfigToBuildConfigWizardPage extends WizardPage {
	private static final String BuildConfigSetKey = "build-config-set"; //$NON-NLS-1$
	private static final String ConfigMapKey = "config-map"; //$NON-NLS-1$
	private static final String SyncConfigSetKey = "sync-config-set"; //$NON-NLS-1$
	private static final String ProjectNameKey = "project-name"; //$NON-NLS-1$

	private String fConfigName;
	
	private Map<String, String> syncConfigToBuildConfigMap = new HashMap<String, String>();
	private Map<String, String> buildConfigNameToIdMap = new HashMap<String, String>();

	enum WizardMode {
		NEW, ADD_SYNC, ADD_CDT
	}
	private final WizardMode wizardMode;

	public SyncConfigToBuildConfigWizardPage(WizardMode mode) {
		super("CDT SyncConfigToBuildConfigWizardPage"); //$NON-NLS-1$
		wizardMode = mode;
		setTitle(Messages.SyncConfigToBuildConfigWizardPage_0); 
		setDescription(Messages.SyncConfigToBuildConfigWizardPage_1); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		String[] syncConfigNames = this.getSyncConfigs();
		String[] buildConfigNames = this.getBuildConfigs();
		//TODO: Set default selections
		for (final String sname : syncConfigNames) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(sname);
			final Combo combo = new Combo(composite, SWT.NONE);
			for (String bname : buildConfigNames) {
				combo.add(bname);
			}
			combo.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = combo.getSelectionIndex();
					if (index >= 0) {
						String buildConfigId = buildConfigNameToIdMap.get(combo.getText());
						syncConfigToBuildConfigMap.put(sname, buildConfigId);
					} else {
						syncConfigToBuildConfigMap.remove(sname);
					}
					SyncWizardDataCache.setMap(getWizard().hashCode(), ConfigMapKey, syncConfigToBuildConfigMap);
				}
			});
		}
		setControl(composite);
		setPageComplete(true);
	}

	public String getBuildConfiguration() {
		return fConfigName;
	}
	
	private String[] getBuildConfigs() {
		switch (wizardMode) {
		case NEW:
		case ADD_CDT:
			Set<String> buildConfigsSet = SyncWizardDataCache.getMultiValueProperty(getWizard().hashCode(), BuildConfigSetKey);
			assert buildConfigsSet != null && buildConfigsSet.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_7;
			return buildConfigsSet.toArray(new String[0]);
		case ADD_SYNC:
			String projectName = SyncWizardDataCache.getProperty(getWizard().hashCode(), ProjectNameKey);
			assert projectName != null : Messages.SyncConfigToBuildConfigWizardPage_3;
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			assert project != null : Messages.SyncConfigToBuildConfigWizardPage_4 + projectName;
			assert RemoteSyncNature.hasNature(project) : Messages.SyncConfigToBuildConfigWizardPage_5 + project.getName();
			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] buildConfigs = buildInfo.getManagedProject().getConfigurations();		
			ArrayList<String> buildConfigNames = new ArrayList<String>();
			for (IConfiguration config : buildConfigs) {
				buildConfigNames.add(config.getName());
				buildConfigNameToIdMap.put(config.getName(), config.getId());
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
			assert syncConfigsSet != null && syncConfigsSet.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_2;
			return syncConfigsSet.toArray(new String[0]);
		case ADD_CDT:
			String projectName = SyncWizardDataCache.getProperty(getWizard().hashCode(), ProjectNameKey);
			assert projectName != null : Messages.SyncConfigToBuildConfigWizardPage_3;
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			assert project != null : Messages.SyncConfigToBuildConfigWizardPage_4 + projectName;
			assert RemoteSyncNature.hasNature(project) : Messages.SyncConfigToBuildConfigWizardPage_5 + project.getName();
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
}