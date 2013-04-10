/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Roland Schulz, University of Tennessee
 * John Eblen, Oak Ridge National Laboratory
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.SyncPluginImages;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.handlers.CommonSyncExceptionHandler;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;

/**
 * A wizard for creating new Synchronized Projects
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class NewRemoteSyncProjectWizard extends CDTCommonProjectWizard {
	private static final String PREFIX = "CProjectWizard"; //$NON-NLS-1$
	private static final String wz_title = Messages.NewRemoteSyncProjectWizard_title;
	private static final String wz_desc = Messages.NewRemoteSyncProjectWizard_description;

	/**
	 * 
	 */
	public NewRemoteSyncProjectWizard() {
		super(wz_title, wz_desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#addPages()
	 */
	@Override
	public void addPages() {
		fMainPage = new SyncMainWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#continueCreation(org
	 * .eclipse.core.resources.IProject)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#getNatures()
	 */
	@Override
	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, RemoteSyncNature.NATURE_ID };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// Disable initial auto build but make sure to set it back to previous value before exiting.
		boolean autoBuildWasSet = setAutoBuild(false);
		boolean success = false;
		IProject project = null;
		try {
			success = super.performFinish();
			if (success) {
				project = this.getProject(true);
				// Uncomment try/catch statements if run is ever changed to spawn a thread.
				// try {
				NewRemoteSyncProjectWizardOperation.run(project, (SyncMainWizardPage) fMainPage, null);
				// } catch (InvocationTargetException e) {
				// success = false;
				// } catch (InterruptedException e) {
				// success = false;
				// }
			}
		} finally {
			setAutoBuild(autoBuildWasSet);
		}

		// Force an initial sync
		if (success && project != null) {
			try {
				SyncManager.sync(null, project, SyncFlag.FORCE, new CommonSyncExceptionHandler(false, true));
			} catch (CoreException e) {
				// This should never happen because only a blocking sync can throw a core exception.
				Activator.log(Messages.NewRemoteSyncProjectWizard_0, e);
			}
		}

		return success;
	}

	// Helper function to disable/enable auto build during project creation
	// Returns the value of auto build before function was called.
	private static boolean setAutoBuild(boolean shouldBeEnabled) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		boolean isAutoBuilding = desc.isAutoBuilding();
		if (isAutoBuilding != shouldBeEnabled) {
			desc.setAutoBuilding(shouldBeEnabled);
			try {
				workspace.setDescription(desc);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return isAutoBuilding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#continueCreation(org
	 * .eclipse.core.resources.IProject)
	 */
	@Override
	protected IProject continueCreation(IProject prj) {
		try {
			CProjectNature.addCNature(prj, new NullProgressMonitor());
			CCProjectNature.addCCNature(prj, new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO: What to do here?
		}
		return prj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#
	 * initializeDefaultPageImageDescriptor()
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(SyncPluginImages.DESC_WIZBAN_NEW_REMOTE_C_PROJ);
	}
}
