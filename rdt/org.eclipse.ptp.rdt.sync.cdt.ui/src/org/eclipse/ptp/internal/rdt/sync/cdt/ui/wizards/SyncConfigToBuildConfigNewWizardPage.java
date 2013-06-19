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
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Page appended to the Synchronize C/C++ or Fortran new project wizard for selecting default build configurations.
 * This class and {@link #SyncConfigToBuildConfigConvertWizardPage} are similar.
 */
public class SyncConfigToBuildConfigNewWizardPage extends WizardPage {
	private static final String ToolchainMapKey = "toolchain-map"; //$NON-NLS-1$
	private static final String SyncConfigSetKey = "sync-config-set"; //$NON-NLS-1$
	public static final String CDT_CONFIG_PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.CConfigWizardPage"; //$NON-NLS-1$

	private String[] syncConfigNames;
	private String[] buildConfigNames;
	private Map<String, String> syncConfigToBuildConfigMap = new HashMap<String, String>();
	private Map<String, String> toolchainToBuildConfigMap = new HashMap<String, String>();

	private Composite parentComposite = null;
	private DefaultBuildConfigWidget configWidget = null;

	public SyncConfigToBuildConfigNewWizardPage() {
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

	private CDTConfigWizardPage findCDTConfigPage() {
		for (IWizardPage page = getPreviousPage(); page != null; page = page.getPreviousPage()) {
			if (page instanceof CDTConfigWizardPage) {
				return (CDTConfigWizardPage) page;
			}
		}
		return null;
	}

	private void getCachedData() {
		getBuildConfigData();
		getSyncConfigData();
		getDefaultMappingData();
	}

	private void getBuildConfigData() {
		CDTConfigWizardPage configPage = findCDTConfigPage();
		if (configPage == null) {
			Activator.log(Messages.SyncConfigToBuildConfigWizardPage_8);
			return;
		}

		ArrayList<String> configNames = new ArrayList<String>();
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
		assert configNames.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_7;
		buildConfigNames = new String[configNames.size()];
		configNames.toArray(buildConfigNames);
	}

	private void getSyncConfigData() {
		Set<String> configNames = SyncWizardDataCache.getMultiValueProperty(SyncConfigSetKey);
		assert configNames != null && configNames.size() > 0 : Messages.SyncConfigToBuildConfigWizardPage_2;
		syncConfigNames = new String[configNames.size()];
		configNames.toArray(syncConfigNames);
	}

	private void getDefaultMappingData() {
		Map<String, String> syncConfigToToolchainMap = SyncWizardDataCache.getMap(ToolchainMapKey);
		if (syncConfigToToolchainMap == null) {
			syncConfigToToolchainMap = new HashMap<String, String>();
		}
		for (Map.Entry<String, String> e : syncConfigToToolchainMap.entrySet()) {
			String syncConfigName = e.getKey();
			String toolchainName = e.getValue();
			if (toolchainName != null) {
				String buildConfigName = toolchainToBuildConfigMap.get(toolchainName);
				if (buildConfigName != null) {
					syncConfigToBuildConfigMap.put(syncConfigName, buildConfigName);
				}
			}
		}
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
	
	private void update() {
		getCachedData();
		if (configWidget != null) {
			configWidget.dispose();
		}
		configWidget = new DefaultBuildConfigWidget(parentComposite, SWT.NONE, syncConfigNames, buildConfigNames,
				syncConfigToBuildConfigMap);
		setControl(configWidget);
		parentComposite.layout(true, true);
	}
}