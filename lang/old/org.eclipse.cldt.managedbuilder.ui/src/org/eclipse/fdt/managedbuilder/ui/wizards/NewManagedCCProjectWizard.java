package org.eclipse.fdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;

public class NewManagedCCProjectWizard extends NewManagedProjectWizard {
	private static final String WZ_TITLE = "MngCCWizard.title";	//$NON-NLS-1$
	private static final String WZ_DESC = "MngCCWizard.description";	//$NON-NLS-1$
	private static final String SETTINGS_TITLE = "MngCCWizardSettings.title";	//$NON-NLS-1$
	private static final String SETTINGS_DESC = "MngCCWizardSettings.description";	//$NON-NLS-1$
	private static final String MSG_CREATE = "MngCCWizard.message.creating";	//$NON-NLS-1$
	
	public NewManagedCCProjectWizard() {
		this(ManagedBuilderUIMessages.getResourceString(WZ_TITLE), ManagedBuilderUIMessages.getResourceString(WZ_DESC));
	}

	public NewManagedCCProjectWizard(String title, String desc) {
		super(title, desc);
	}

	public void addPages() {
		// Add the default page for all new managed projects 
		super.addPages();
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(ManagedBuilderUIMessages.getResourceString(MSG_CREATE), 8); //$NON-NLS-1$
		super.doRun(new SubProgressMonitor(monitor, 7));
		// Add C++ Nature.
		if (newProject != null) {
			// Add C++ Nature to the newly created project.
			FortranCorePlugin.getDefault().convertProjectFromCtoCC(newProject, new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
	}
}
