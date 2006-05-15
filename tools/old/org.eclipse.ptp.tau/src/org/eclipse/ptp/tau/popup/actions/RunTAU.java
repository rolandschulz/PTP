/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/

package org.eclipse.ptp.tau.popup.actions;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.tau.popup.actions.TAUApplicationLaunchShortcut;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RunTAU implements IObjectActionDelegate {
	/**
	 * Constructor for Action1.
	 */
	public RunTAU() {
		super();
	}
	IStructuredSelection selection;
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if(selection==null)return;
		/*Collect the binary object and check for viability
		 * Collect the run mode.  Create a new TAU launcher
		 * and launch the binary.*/
		IBinary bin = (IBinary) selection.getFirstElement();
		if (bin == null){
			System.out.println("No project!");
			return;
		}
		String mode = ILaunchManager.RUN_MODE;
		TAUApplicationLaunchShortcut cut = new TAUApplicationLaunchShortcut();
		cut.launch(bin,mode);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) selection;
		else
		{	//if the selection is invalid, stop
			this.selection = null;
			System.out.println("Invalid Selection");
		}
	}
}