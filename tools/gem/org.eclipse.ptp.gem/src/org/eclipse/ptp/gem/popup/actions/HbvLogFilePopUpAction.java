/*******************************************************************************
 * Copyright (c) 2009, 2010 University of Utah School of Computing
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
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

public class HbvLogFilePopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 */
	public HbvLogFilePopUpAction() {
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

		// Make sure the file is valid
		if (this.selection.toString().equals("<empty selection>")) { //$NON-NLS-1$
			GemUtilities.showErrorDialog(Messages.HbvLogFilePopUpAction_1,
					Messages.HbvLogFilePopUpAction_2);
		}
		String hbvCommand = GemPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.GEM_PREF_HBV_PATH);
		if (hbvCommand != "") { //$NON-NLS-1$
			hbvCommand += "/"; //$NON-NLS-1$
		}
		GemUtilities.runCommandAsThread("ispUI " + logFilePath); //$NON-NLS-1$
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
