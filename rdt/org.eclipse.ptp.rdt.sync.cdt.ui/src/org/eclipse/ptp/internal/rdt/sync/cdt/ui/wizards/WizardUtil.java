/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;

/**
 * Utility class for common CDT operations needed by wizards.
 */
public class WizardUtil {
	private static final String SYNC_BUILDER_CLASS = "org.eclipse.ptp.rdt.sync.cdt.core.SyncBuilder"; //$NON-NLS-1$

	// Map from CDT language settings providers to their sync replacements. The list of providers is modified only by swapping
	// the keys with their corresponding values. If the value is null, the provider is simply removed.
	// TODO: If ever needed, provide a mechanism to add a provider without replacing one.
	private static final Map<String, String> languageSettingsProviderReplacementsMap;
	static {
		languageSettingsProviderReplacementsMap = new HashMap<String, String>();
		languageSettingsProviderReplacementsMap.put("org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector", //$NON-NLS-1$
				"org.eclipse.ptp.rdt.sync.core.SyncGCCBuiltinSpecsDetector"); //$NON-NLS-1$
		languageSettingsProviderReplacementsMap.put("org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser", //$NON-NLS-1$
				"org.eclipse.ptp.rdt.sync.core.SyncGCCBuildCommandParser"); //$NON-NLS-1$
	}

	/**
	 * Modifications to all build configurations, which are necessary for synchronized projects.
	 * Changes are also saved to CDT.
	 *
	 * @param buildConfig
	 * 				build configuration to modify - cannot be null
	 */
	public static void modifyBuildConfigForSync(IConfiguration buildConfig) {
		IBuilder syncBuilder = ManagedBuildManager.getExtensionBuilder(SYNC_BUILDER_CLASS);

		// Set all configs to use the sync builder, which ensures the build always occurs at the active sync config location.
		buildConfig.changeBuilder(syncBuilder, SYNC_BUILDER_CLASS, Messages.WizardUtil_0);
		
		ManagedBuildManager.saveBuildInfo(buildConfig.getOwner().getProject(), true);
	}

	/**
	 * Modifications to local build configurations only, which are necessary for synchronized projects.
	 * Changes are also saved to CDT.
	 *
	 * @param buildConfig
	 * 				build configuration to modify - cannot be null
	 */
	public static void modifyLocalBuildConfigForSync(IConfiguration buildConfig) {
		// none currently
	}

	/**
	 * Modifications to remote build configurations only, which are necessary for synchronized projects.
	 * Changes are also saved to CDT.
	 *
	 * @param buildConfig
	 * 				build configuration to modify - cannot be null
	 */
	public static void modifyRemoteBuildConfigForSync(IConfiguration buildConfig) {
		// turn off append contributed (local) environment variables
		ICConfigurationDescription c_mb_confgDes = ManagedBuildManager.getDescriptionForConfiguration(buildConfig);
		if (c_mb_confgDes != null) {
			EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(false, c_mb_confgDes);
		}
		
		ManagedBuildManager.saveBuildInfo(buildConfig.getOwner().getProject(), true);

		// Replace CDT language setting providers with sync versions
		// We first have to get a writable description. The description used earlier is read-only.
		ICProjectDescription projectDesc = CoreModel.getDefault().getProjectDescription(buildConfig.getOwner().getProject());
		ICConfigurationDescription configDesc = projectDesc.getConfigurationById(buildConfig.getId());
		if (configDesc == null) {
			Activator.log(Messages.WizardUtil_1 + buildConfig.getName());
		}

		// Now do the actual replacing
		ILanguageSettingsProvidersKeeper lspk = ((ILanguageSettingsProvidersKeeper) configDesc);
		List<ILanguageSettingsProvider> oldProviders = lspk.getLanguageSettingProviders();
		List<ILanguageSettingsProvider> newProviders = new ArrayList<ILanguageSettingsProvider>();
		for (ILanguageSettingsProvider p : oldProviders) {
			if (languageSettingsProviderReplacementsMap.containsKey(p.getId())) {
				String replacementId = languageSettingsProviderReplacementsMap.get(p.getId());
				if (replacementId == null) {
					continue;
				}
				p = LanguageSettingsManager.getWorkspaceProvider(replacementId);
			}
			newProviders.add(p);
		}
		lspk.setLanguageSettingProviders(newProviders);

		// and save results
		buildConfig.setDirty(true);
		setProjectDescription(buildConfig.getOwner().getProject(), projectDesc);

	}
	
    /**
     * Writing to the .cproject file fails if the workspace is locked. So calling CoreModel.getDefault().setProjectDescription() is
     * not enough. Instead, spawn a thread that calls this function once the workspace is unlocked.
     *
     * @param project
     * @param desc
     */
    public static void setProjectDescription(final IProject project, final ICProjectDescription desc) {
            Throwable firstException = null;
            final IWorkspace ws = ResourcesPlugin.getWorkspace();
            // Avoid creating a thread if possible.
            try {
                    if (!ws.isTreeLocked()) {
                            CoreModel.getDefault().setProjectDescription(project, desc, true, null);
                            return;
                    }
            } catch (CoreException e) {
                    // This can happen in the rare case that the lock is locked between the check and the flush but also for other reasons.
                    // Be optimistic and proceed to create thread.
                    firstException = e;
            }

            final Throwable currentException = firstException;
            Thread flushThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                            int sleepCount = 0;
                            Throwable lastException = currentException;
                            while (true) {
                                    try {
                                            Thread.sleep(1000);
                                            // Give up after 30 sleeps - this should never happen
                                            sleepCount++;
                                            if (sleepCount > 30) {
                                                    if (lastException != null) {
                                                            Activator.log(Messages.WizardUtil_2, lastException);
                                                    } else {
                                                            Activator.log(Messages.WizardUtil_2);
                                                    }
                                                    break;
                                            }
                                            if (!ws.isTreeLocked()) {
                                                    CoreModel.getDefault().setProjectDescription(project, desc, true, null);
                                                    break;
                                            }
                                    } catch (InterruptedException e) {
                                            lastException = e;
                                    } catch (CoreException e) {
                                            // This can happen in the rare case that the lock is locked between the check and the flush but also for
                                            // other reasons.
                                            // Be optimistic and try again.
                                            lastException = e;
                                    }
                            }
                    }
            }, "Save project CDT data thread"); //$NON-NLS-1$
            flushThread.start();
    }
}
