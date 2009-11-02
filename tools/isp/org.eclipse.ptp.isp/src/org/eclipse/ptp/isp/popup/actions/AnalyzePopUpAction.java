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
import org.eclipse.ptp.isp.views.ISPConsole;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class AnalyzePopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 */
	public AnalyzePopUpAction() {
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
		String sourceFilePath = s.toPortableString();

		// Make sure the file selection is valid
		if (this.selection.toString().equals("<empty selection>")) { //$NON-NLS-1$
			IspUtilities.showErrorDialog(Messages.AnalyzePopUpAction_0,
					Messages.AnalyzePopUpAction_1);
		} else {
			String id = action.getId();
			IspUtilities.saveLastFile(sourceFilePath);

			// Check if the log file exists
			String logFilePath = IspUtilities.getLogFile(sourceFilePath);

			// Find the active page
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			// Open the Console View
			try {
				page.showView(ISPConsole.ID);
			} catch (PartInitException pie) {
				IspUtilities.showExceptionDialog(Messages.AnalyzePopUpAction_2,
						pie);
				IspUtilities.logError(Messages.AnalyzePopUpAction_2, pie);
			}

			// Open the Analyzer View
			try {
				page.showView(ISPAnalyze.ID);
				IspUtilities
						.activateAnalyzer(
								sourceFilePath,
								logFilePath,
								id.equals("org.eclipse.ptp.isp.analyzePopup"), id.equals("org.eclipse.ptp.isp.executablePopup")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (PartInitException pie) {
				IspUtilities.showExceptionDialog(Messages.AnalyzePopUpAction_2,
						pie);
				IspUtilities.logError(Messages.AnalyzePopUpAction_2, pie);
			}
		}
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