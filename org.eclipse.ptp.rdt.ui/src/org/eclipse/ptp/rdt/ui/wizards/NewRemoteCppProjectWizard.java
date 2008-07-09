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
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.ui.messages.Messages;

/**
 * @author crecoskie
 *
 */
public class NewRemoteCppProjectWizard extends CDTCommonProjectWizard {

	/**
	 * 
	 */
	public NewRemoteCppProjectWizard() {
		super(Messages.getString("NewRemoteCppProjectWizard_0"), Messages.getString("NewRemoteCppProjectWizard_1")); //$NON-NLS-1$ //$NON-NLS-2$
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


}
