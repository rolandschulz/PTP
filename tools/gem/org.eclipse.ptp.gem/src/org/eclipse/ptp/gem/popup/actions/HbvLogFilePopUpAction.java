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

package org.eclipse.ptp.gem.popup.actions;

//import java.io.File;

//import org.eclipse.core.filesystem.EFS;
//import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.gem.util.GemUtilities;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Object action implementation for associated GEM actions declared in
 * plugin.xml.
 * 
 * Implements IObjectActionDelegate, which extends ActionDelegate, an
 * IActionDelegate base class.
 * 
 * @see org.eclipse.ui.IObjectActionDelegate
 */
public class HbvLogFilePopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 */
	public HbvLogFilePopUpAction() {
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
			GemUtilities.showErrorDialog(Messages.HbvLogFilePopUpAction_0);
		} else {
			final IFile logFile = (IFile) this.selection.getFirstElement();
			final String projectName = logFile.getProject().getName();
			final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			String hbv = GemPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GEM_PREF_HBV_PATH);

			/*
			 * Currently, GEM does not support the Happens Before Viewer with
			 * remote projects.
			 * 
			 * Check if the project is local or remote and abort if it is.
			 */
			if (logFile.getLocation() == null || GemUtilities.isSynchronizedProject(logFile)) {
				GemUtilities.showInformationDialog(Messages.HbvLogFilePopUpAction_2);
				return;
			}

			// Sync the project resources with the underlying file system
			GemUtilities.refreshProject(currentProject);

			// Check the log file and execute the command via thread
			if (logFile.exists()) {
				if (hbv != "") { //$NON-NLS-1$
					hbv += "/"; //$NON-NLS-1$
				}
				hbv += "ispUI " + logFile.getLocationURI().getPath(); //$NON-NLS-1$
				GemUtilities.runCommandAsThread(hbv);
			} else {
				GemUtilities.showErrorDialog(Messages.HbvLogFilePopUpAction_1);
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
