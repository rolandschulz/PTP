/*******************************************************************************
 * Copyright (c) 2006,20078 IBM Corp. and others.
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
 * Wizard Page for collecting info about MPI C project - specifics for C vs C++
 * 
 * @author Beth Tibbitts
 * 
 */
public class MPIProjectWizardPageC extends MPIProjectWizardPage {

	public MPIProjectWizardPageC() throws CoreException {
		super();
	}

	@Override
	protected String getMpiProjectType() {
		return MPI_PROJECT_TYPE_C;
	}

	@Override
	protected String getDefaultMpiBuildCommand() {
		String cmd = preferenceStore.getString(MpiIDs.MPI_BUILD_CMD);
		return cmd;
	}
	

}
