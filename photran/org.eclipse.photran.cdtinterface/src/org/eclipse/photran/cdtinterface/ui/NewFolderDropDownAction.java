/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.internal.ui.wizards.AbstractWizardDropDownAction;
import org.eclipse.jface.action.IAction;

/**
 * This file copied from org.eclipse.cdt.internal.ui.wizards.NewFolderDropDownAction (1.2)
 * 
 * @author C.E.Rasmussen
 */
public class NewFolderDropDownAction extends AbstractWizardDropDownAction
{
	public NewFolderDropDownAction()
	{
	    super();
//		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_FOLDER_WIZARD_ACTION);
	}

	protected IAction[] getWizardActions()
	{
		return FortranWizardRegistry.getFolderWizardActions();
	}
}
