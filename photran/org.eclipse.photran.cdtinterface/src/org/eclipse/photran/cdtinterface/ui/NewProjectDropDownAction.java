/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.wizards.AbstractWizardDropDownAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

/**
 * This file copied from org.eclipse.cdt.internal.ui.wizards.NewProjectDropDownAction (1.4)
 * 
 * @author C.E.Rasmussen
 */
public class NewProjectDropDownAction extends AbstractWizardDropDownAction
{
	public NewProjectDropDownAction()
	{
	    super();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.OPEN_PROJECT_WIZARD_ACTION);
	}
	
	protected IAction[] getWizardActions()
	{
		return FortranWizardRegistry.getProjectWizardActions();
	}
}
