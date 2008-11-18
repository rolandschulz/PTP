/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.internal.rdt.ui.RDTPluginImages;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.swt.widgets.Composite;

/**
 * A wizard for creating new Remote C/C++ Projects
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class NewRemoteCppProjectWizard extends CDTCommonProjectWizard {
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String wz_title = Messages.getString("NewRemoteCppProjectWizard_0"); //$NON-NLS-1$
	private static final String wz_desc = Messages.getString("NewRemoteCppProjectWizard_1"); //$NON-NLS-1$
	
	/**
	 * 
	 */
	public NewRemoteCppProjectWizard() {
		
		super(wz_title, wz_desc);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#continueCreation(org.eclipse.core.resources.IProject)
	 */
	
	@Override
	protected IProject continueCreation(IProject prj) {
		try {
			CProjectNature.addCNature(prj, new NullProgressMonitor());
			CCProjectNature.addCCNature(prj, new NullProgressMonitor());
			RemoteNature.addRemoteNature(prj, new NullProgressMonitor());
		} catch (CoreException e) {}
		return prj;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#getNatures()
	 */
	@Override
	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, RemoteNature.REMOTE_NATURE_ID};
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#initializeDefaultPageImageDescriptor()
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(RDTPluginImages.DESC_WIZBAN_NEW_REMOTE_C_PROJ);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#addPages()
	 */
	@Override
	public void addPages() {
		fMainPage= new RDTMainWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}
	
	

}
