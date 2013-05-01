/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
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

package org.eclipse.ptp.internal.gem.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.gem.GemPlugin;
import org.eclipse.ptp.internal.gem.messages.Messages;
import org.eclipse.ptp.internal.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.internal.gem.util.GemUtilities;
import org.eclipse.ptp.internal.gem.views.GemAnalyzer;
import org.eclipse.ptp.internal.gem.views.GemBrowser;
import org.eclipse.ptp.internal.gem.views.GemConsole;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Object action implementation for associated GEM actions declared in
 * plugin.xml.
 * 
 * Implements IObjectActionDelegate, which extends ActionDelegate, an
 * IActionDelegate base class.
 * 
 * @see org.eclipse.ui.IObjectActionDelegate
 */
public class ProcessLogFilePopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 */
	public ProcessLogFilePopUpAction() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (this.selection == null) {
			return;
		}

		// Make sure the file selection is valid
		if (this.selection.toString().equals("<empty selection>")) { //$NON-NLS-1$
			GemUtilities.showErrorDialog(Messages.ProcessLogFilePopUpAction_0);
		} else {
			final IFile logFile = (IFile) this.selection.getFirstElement();
			final IWorkbench wb = PlatformUI.getWorkbench();
			final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			final IWorkbenchPage page = window.getActivePage();
			final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();

			// Open GEM views in order determined by preference
			try {
				final String activeView = pstore.getString(PreferenceConstants.GEM_ACTIVE_VIEW);
				if (activeView.equals("analyzer")) { //$NON-NLS-1$
					page.showView(GemBrowser.ID);
					page.showView(GemAnalyzer.ID);
				} else {
					page.showView(GemAnalyzer.ID);
					page.showView(GemBrowser.ID);
				}
				page.showView(GemConsole.ID);
			} catch (final PartInitException e) {
				GemUtilities.logExceptionDetail(e);
			}

			// Sync the project resources with the underlying file system
			final String projectName = logFile.getProject().getName();
			final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			GemUtilities.refreshProject(currentProject);

			// Check the log file and execute the command via thread
			if (logFile.exists()) {
				GemUtilities.initGemViews(logFile, false, false);
			} else {
				GemUtilities.showErrorDialog(Messages.ProcessLogFilePopUpAction_1);
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

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// do nothing
	}

}
