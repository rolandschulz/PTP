/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.ui.wizards;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.ui.CUIPlugin;


/**
 * C Project wizard that creates a new project resource in
 */
public abstract class NewCCProjectWizard extends NewCProjectWizard {

	public NewCCProjectWizard() {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	public NewCCProjectWizard(String title, String description) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}


	protected void doRun(IProgressMonitor monitor) throws CoreException {
		super.doRun(monitor);
		// Add C++ Nature to the newly created project.
        if (newProject != null){
            FortranCorePlugin.getDefault().convertProjectFromCtoCC(newProject, monitor);
        }
	}
}
