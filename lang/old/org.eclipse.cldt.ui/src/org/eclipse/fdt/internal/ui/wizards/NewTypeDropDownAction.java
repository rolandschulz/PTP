/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.ui.wizards;

import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.help.WorkbenchHelp;

public class NewTypeDropDownAction extends AbstractWizardDropDownAction {

	public NewTypeDropDownAction() {
	    super();
		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}

	protected IAction[] getWizardActions() {
		return FortranWizardRegistry.getTypeWizardActions();
	}
}
