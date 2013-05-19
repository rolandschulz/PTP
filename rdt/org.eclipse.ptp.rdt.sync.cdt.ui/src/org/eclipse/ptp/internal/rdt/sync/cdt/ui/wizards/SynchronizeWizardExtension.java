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
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.SyncConfigToBuildConfigWizardPage.WizardMode;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.ui.AbstractSynchronizeWizardExtension;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardExtensionDescriptor;

/**
 * Synchronize conversion wizard extension for converting a CDT project to a synchronized CDT project
 */
public class SynchronizeWizardExtension extends AbstractSynchronizeWizardExtension {
    private static final String DEFAULT_BUILD_CONFIG_ID = "default-build-config-id"; //$NON-NLS-1$
	private static final String ProjectNameKey = "project-name"; //$NON-NLS-1$
	private static final String ConfigMapKey = "config-map"; //$NON-NLS-1$

	private SyncConfigToBuildConfigWizardPage fWizardPage;

	public SynchronizeWizardExtension(ISynchronizeWizardExtensionDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public WizardPage createConvertProjectWizardPage() {
		fWizardPage = new SyncConfigToBuildConfigWizardPage(WizardMode.ADD_SYNC);
		return fWizardPage;
	}

    private IProject getAndValidateProject() {
        String projectName = SyncWizardDataCache.getProperty(ProjectNameKey);
        if (projectName == null) {
            return null;
        }
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        assert project != null : Messages.SynchronizeWizardExtension_0 + projectName;
        return project;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardExtension#performFinish()
	 */
	@Override
	public void performFinish() {
		IProject project = this.getAndValidateProject();
        assert isSyncProject(project) && isCDTProject(project) : Messages.SynchronizeWizardExtension_1 + project.getName();

        Map<String, String> configMap = SyncWizardDataCache.getMap(ConfigMapKey);
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
}