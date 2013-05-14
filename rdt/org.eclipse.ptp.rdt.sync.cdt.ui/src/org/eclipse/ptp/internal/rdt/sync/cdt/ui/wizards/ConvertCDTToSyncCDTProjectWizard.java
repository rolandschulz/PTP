/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.ConvertToMakeWizardPage;
import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.SyncConfigToBuildConfigWizardPage.WizardMode;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;

/**
 * A wizard for converting CDT projects to Sync + CDT projects (Synchronized C/C++ projects)
 * This is a subclass of the CDT wizard that simply adds an additional page and data.
 */
public class ConvertCDTToSyncCDTProjectWizard extends ConversionWizard {
	private static final String BuildConfigSetKey = "build-config-set"; //$NON-NLS-1$
	private static final String wz_title = Messages.ConvertLocalToSyncProjectWizard_0;
	private static final String wz_desc = Messages.ConvertLocalToSyncProjectWizard_1;

	/**
	 * 
	 */
	public ConvertCDTToSyncCDTProjectWizard() {
		super(wz_title, wz_desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#addPages()
	 */
	@Override
	public void addPages() {
	    mainPage = new ConvertToMakeWizardPage(getPrefix());
	    addPage(mainPage);
		IWizardPage configMapPage = new SyncConfigToBuildConfigWizardPage(WizardMode.ADD_CDT);
		addPage(configMapPage);
	}

    @Override
    public String getProjectID() {
        return "org.eclipse.ptp.rdt.sync"; //$NON-NLS-1$
    }
    
    @Override
    public boolean performFinish() {
    	boolean success = super.performFinish();
    	if (!success) {
    		return false;
    	}

    	IProject project = this.getNewProject();
    	assert project != null : Messages.ConvertLocalToSyncProjectWizard_2;
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		assert buildInfo != null : Messages.ConvertLocalToSyncProjectWizard_3 + project.getName();
		IConfiguration[] buildConfigs = buildInfo.getManagedProject().getConfigurations();		
		Set<String> buildConfigNames = new HashSet<String>();
		for (IConfiguration config : buildConfigs) {
			buildConfigNames.add(config.getName());
			SyncWizardDataCache.setMultiValueProperty(this.hashCode(), BuildConfigSetKey, buildConfigNames);
		}
    	return true;
    }
}