/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.preferences;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ui.statushandlers.StatusManager;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * This tries to give a good default value for the target project for importing
	 */
	private String calcDefaultTargetProject() {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot root = workspace.getRoot();
		final IProject[] projects = root.getProjects();
		if (projects.length > 0) {
			final IProject project = projects[0];
			return project.getName();
		}
		else {
			final String gklee = "Gklee"; //$NON-NLS-1$
			final IProject project = root.getProject(gklee);
			try {
				project.create(null);
				project.open(null);
				root.refreshLocal(1, null);
				return gklee;
			} catch (final CoreException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
				return gklee;
			}
		}
	}

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = GIGPlugin.getDefault().getPreferenceStore();

		// find the HOME directory as a start
		ProcessBuilder processBuilder = new ProcessBuilder();
		Map<String, String> env = processBuilder.environment();
		final String home = env.get("HOME"); //$NON-NLS-1$
		processBuilder = null;
		env = null;

		// assume default installation directories
		final String gkleeHome = home + "/gklee"; //$NON-NLS-1$
		final String flaKleeHomeDir = gkleeHome;
		final String gkleeDebugPlusAssertsBin = gkleeHome + "/Gklee/Debug+Asserts/bin"; //$NON-NLS-1$
		final String llvmDebugPlusAssertsBin = gkleeHome + "/llvm-2.8/Debug+Asserts/bin"; //$NON-NLS-1$
		final String llvmGccLinuxBin = gkleeHome + "/llvm-gcc4.2-2.8-x86_64-linux/bin"; //$NON-NLS-1$
		final String bin = gkleeHome + "/bin"; //$NON-NLS-1$
		final String otherPATH = ""; //$NON-NLS-1$

		preferenceStore.setDefault(GIGPreferencePage.LOCAL, false);
		preferenceStore.setDefault(Messages.TARGET_PROJECT, calcDefaultTargetProject());
		preferenceStore.setDefault(GIGPreferencePage.USERNAME, "gklee"); //$NON-NLS-1$
		preferenceStore.setDefault(GIGPreferencePage.PASSWORD, ""); //$NON-NLS-1$
		preferenceStore.setDefault(Messages.SERVER_NAME, "formal.cs.utah.edu"); //$NON-NLS-1$
		preferenceStore.setDefault(GIGPreferencePage.GKLEE_HOME, gkleeHome);
		preferenceStore.setDefault(GIGPreferencePage.FLA_KLEE_HOME_DIR, flaKleeHomeDir);
		preferenceStore.setDefault(GIGPreferencePage.GKLEE_DEBUG_PLUS_ASSERTS_BIN, gkleeDebugPlusAssertsBin);
		preferenceStore.setDefault(GIGPreferencePage.LLVM_DEBUG_PLUS_ASSERTS_BIN, llvmDebugPlusAssertsBin);
		preferenceStore.setDefault(GIGPreferencePage.LLVM_GCC_LINUX_BIN, llvmGccLinuxBin);
		preferenceStore.setDefault(GIGPreferencePage.BIN, bin);
		preferenceStore.setDefault(GIGPreferencePage.ADDITIONAL_PATH, otherPATH);

		preferenceStore.setDefault(Messages.BANK_OR_WARP, false);
		preferenceStore.setDefault(Messages.BANK_CONFLICT_LOW, 0);
		preferenceStore.setDefault(Messages.BANK_CONFLICT_HIGH, 50);
		preferenceStore.setDefault(Messages.MEMORY_COALESCING_LOW, 50);
		preferenceStore.setDefault(Messages.MEMORY_COALESCING_HIGH, 10);
		preferenceStore.setDefault(Messages.WARP_DIVERGENCE_LOW, 0);
		preferenceStore.setDefault(Messages.WARP_DIVERGENCE_HIGH, 50);
	}
}
