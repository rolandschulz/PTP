/*******************************************************************************
 * Copyright (c) 2006,2008 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;


/**
 * Wizard Page for collecting info about MPI C++ project 
 * 
 * @author Beth Tibbitts
 * 
 */
public class MPIProjectWizardPageCPP extends MPIProjectWizardPage {

	public MPIProjectWizardPageCPP() throws CoreException {
		super();
	}

	@Override
	protected String getMpiProjectType() {
		return MPI_PROJECT_TYPE_CPP;
	}
	
	@Override
	protected String getDefaultMpiBuildCommand() {
		String cmd = preferenceStore.getString(MpiIDs.MPI_CPP_BUILD_CMD);
		return cmd;
	}
	

}
