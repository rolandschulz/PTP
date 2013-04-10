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
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.fortran.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.photran.internal.cdtinterface.ui.FortranProjectWizard;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.NewRemoteSyncProjectWizardOperation;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.SyncMainWizardPage;
import org.eclipse.ptp.internal.rdt.sync.fortran.ui.PluginImages;
import org.eclipse.ptp.internal.rdt.sync.fortran.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;

/**
 * A wizard for creating new Fortran synchronized projects
 * 
 */
public class NewFortranSyncProjectWizard extends FortranProjectWizard {
	private static final String PREFIX = "CProjectWizard"; //$NON-NLS-1$
	private static final String wz_title = Messages.NewFortranSyncProjectWizard_new_sync_fortran_project;
	private static final String wz_desc = Messages.NewFortranSyncProjectWizard_create_sync_fortran_project;

	/**
	 * 
	 */
	public NewFortranSyncProjectWizard() {
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
		List<String> natures = new ArrayList<String>();
		natures.addAll(Arrays.asList(super.getNatures()));
		natures.add(RemoteSyncNature.NATURE_ID);
		return natures.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean success = super.performFinish();
		if (success) {
			IProject project = this.getProject(true);
			// Uncomment try/catch statements if run is ever changed to spawn a thread.
			// try {
			NewRemoteSyncProjectWizardOperation.run(project, (SyncMainWizardPage) fMainPage, null);
			// } catch (InvocationTargetException e) {
			// success = false;
			// } catch (InterruptedException e) {
			// success = false;
			// }
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.photran.internal.cdtinterface.ui.FortranProjectWizard#
	 * continueCreation(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected IProject continueCreation(IProject prj) {
		super.continueCreation(prj);
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
		setDefaultPageImageDescriptor(PluginImages.DESC_WIZBAN_NEW_REMOTE_C_PROJ);
	}
}
