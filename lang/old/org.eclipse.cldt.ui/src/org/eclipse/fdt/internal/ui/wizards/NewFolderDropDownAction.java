/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.internal.ui.wizards;

import org.eclipse.jface.action.IAction;

public class NewFolderDropDownAction extends AbstractWizardDropDownAction {

	public NewFolderDropDownAction() {
	    super();
//		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_FOLDER_WIZARD_ACTION);
	}

	protected IAction[] getWizardActions() {
		return CWizardRegistry.getFolderWizardActions();
	}
}
