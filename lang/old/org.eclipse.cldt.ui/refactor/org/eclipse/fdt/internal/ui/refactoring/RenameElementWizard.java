/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.ui.refactoring;

import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.internal.ui.refactoring.RefactoringMessages;

public class RenameElementWizard extends RenameRefactoringWizard {
	public RenameElementWizard() {
		super(
			RefactoringMessages.getString("RenameTypeWizard.defaultPageTitle"), //$NON-NLS-1$
			RefactoringMessages.getString("RenameTypeWizard.inputPage.description"), //$NON-NLS-1$
			FortranPluginImages.DESC_WIZBAN_REFACTOR_TYPE,
			ICHelpContextIds.RENAME_TYPE_WIZARD_PAGE);
	}
}
