/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author ???
 */
public class FortranProjectWizard extends CDTCommonProjectWizard
{

	public FortranProjectWizard()
	{
		super(UIMessages.getString("Fortran Project"),  //$NON-NLS-1$
			  UIMessages.getString("Create Fortran project of selected type")); //$NON-NLS-1$
	}

	public String[] getNatures()
	{
		return new String[] { CProjectNature.C_NATURE_ID };
	}

	protected IProject continueCreation(IProject prj)
	{
		try
		{
			CProjectNature.addCNature(prj, new NullProgressMonitor());
		}
		catch (CoreException e) {}
		
		return prj;
	}

}
