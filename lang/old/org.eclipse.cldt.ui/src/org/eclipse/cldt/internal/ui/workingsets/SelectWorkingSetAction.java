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
package org.eclipse.cldt.internal.ui.workingsets;

import org.eclipse.cldt.internal.ui.ICHelpContextIds;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Displays an IWorkingSetSelectionDialog and sets the selected 
 * working set in the action group's view.
 * 
 * @since 2.0
 */
public class SelectWorkingSetAction extends Action {
	private Shell fShell;
	private WorkingSetFilterActionGroup fActionGroup;

	public SelectWorkingSetAction(WorkingSetFilterActionGroup actionGroup, Shell shell) {
		super(WorkingSetMessages.getString("SelectWorkingSetAction.text")); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		setToolTipText(WorkingSetMessages.getString("SelectWorkingSetAction.toolTip")); //$NON-NLS-1$
		
		fShell= shell;
		fActionGroup= actionGroup;
		WorkbenchHelp.setHelp(this, ICHelpContextIds.SELECT_WORKING_SET_ACTION);
	}
	
	/*
	 * Overrides method from Action
	 */
	public void run() {
		if (fShell == null)
			fShell= FortranUIPlugin.getActiveWorkbenchShell();
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog= manager.createWorkingSetSelectionDialog(fShell, false);
		IWorkingSet workingSet= fActionGroup.getWorkingSet();
		if (workingSet != null)
			dialog.setSelection(new IWorkingSet[]{workingSet});

		if (dialog.open() == Window.OK) {
			IWorkingSet[] result= dialog.getSelection();
			if (result != null && result.length > 0) {
				fActionGroup.setWorkingSet(result[0], true);
				manager.addRecentWorkingSet(result[0]);
			}
			else
				fActionGroup.setWorkingSet(null, true);
		}
	}
}
