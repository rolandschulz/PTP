/*******************************************************************************
 * Copyright (c) 2009, 2011 University of Utah School of Computing
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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.gem.util.GemUtilities;
import org.eclipse.ptp.gem.views.GemAnalyzer;
import org.eclipse.ptp.gem.views.GemBrowser;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
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
public class VerificationPopUpAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor.
	 */
	public VerificationPopUpAction() {
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
			GemUtilities.showErrorDialog(Messages.VerificationPopUpAction_0);
		} else {
			final IFile inputFile = (IFile) this.selection.getFirstElement();
			final String id = action.getId();
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			final IWorkbenchPage page = window.getActivePage();
			final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();

			// Open GEM views
			try {
				final String activeView = pstore.getString(PreferenceConstants.GEM_ACTIVE_VIEW);
				if (activeView.equals("analyzer")) { //$NON-NLS-1$
					page.showView(GemBrowser.ID);
					page.showView(GemAnalyzer.ID);
				} else {
					page.showView(GemAnalyzer.ID);
					page.showView(GemBrowser.ID);
				}
				final boolean isValidSourceFile = id
						.equals("org.eclipse.ptp.gem.verificationPopupC") //$NON-NLS-1$
						|| id.equals("org.eclipse.ptp.gem.verificationPopupCpp") //$NON-NLS-1$
						|| id.equals("org.eclipse.ptp.gem.verificationPopupC++") //$NON-NLS-1$
						|| id.equals("org.eclipse.ptp.gem.verificationPopupCp") //$NON-NLS-1$
						|| id.equals("org.eclipse.ptp.gem.verificationPopupCc"); //$NON-NLS-1$

				// if !isValidSourceFile, then its a .gem profiled executable
				if (isValidSourceFile) {
					GemUtilities.saveMostRecentURI(inputFile.getLocationURI());
				}
				GemUtilities.initGemViews(inputFile, isValidSourceFile, true);
			} catch (final PartInitException e) {
				GemUtilities.logExceptionDetail(e);
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
	}

}
