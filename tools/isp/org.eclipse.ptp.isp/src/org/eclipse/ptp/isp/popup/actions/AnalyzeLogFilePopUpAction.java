/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.isp.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.isp.messages.Messages;
import org.eclipse.ptp.isp.util.IspUtilities;
import org.eclipse.ptp.isp.views.ISPAnalyze;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class AnalyzeLogFilePopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 */
	public AnalyzeLogFilePopUpAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (this.selection == null) {
			return;
		}

		IFile f = (IFile) this.selection.getFirstElement();
		IPath s = f.getLocation();
		String logFilePath = s.toPortableString();
		String sourceFilePath = IspUtilities.getSourcePathFromLog(logFilePath);

		// Find the active file
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		// Open the Analyzer View
		try {
			page.showView(ISPAnalyze.ID);
		} catch (PartInitException pie) {
			IspUtilities.showExceptionDialog(
					Messages.AnalyzeLogFilePopUpAction_1, pie);
			IspUtilities.logError(Messages.AnalyzeLogFilePopUpAction_1, pie);
		}
		IspUtilities
				.activateAnalyzer(sourceFilePath, logFilePath, false, false);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		} else {
			this.selection = null;
		}
	}

}
