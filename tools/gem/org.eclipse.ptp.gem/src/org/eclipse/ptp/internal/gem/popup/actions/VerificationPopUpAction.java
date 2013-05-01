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

import java.net.URI;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
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

		// Check that the selection is valid
		if (this.selection.toString().equals("<empty selection>")) { //$NON-NLS-1$
			GemUtilities.showErrorDialog(Messages.VerificationPopUpAction_0);
		} else {
			final Object selectionElement = this.selection.getFirstElement();
			IResource resource = null;

			if (selectionElement instanceof IResource) {
				resource = (IResource) selectionElement;
			} else if (selectionElement instanceof ICElement) {
				resource = ((ICElement) selectionElement).getResource();
			}

			// Do the work if the resource is valid
			if (resource != null) {
				final String id = action.getId();
				final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage page = window.getActivePage();
				final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();

				// Open GEM views in order determined by preference
				try {
					// ask for command line arguments
					GemUtilities.setCommandLineArgs();

					final String activeView = pstore.getString(PreferenceConstants.GEM_ACTIVE_VIEW);
					if (activeView.equals("analyzer")) { //$NON-NLS-1$
						page.showView(GemBrowser.ID);
						page.showView(GemAnalyzer.ID);
					} else {
						page.showView(GemAnalyzer.ID);
						page.showView(GemBrowser.ID);
					}
					page.showView(GemConsole.ID);

					// if !isValidSourceFile, then its a .gem profiled executable
					final boolean isValidSourceFile = id.equals("org.eclipse.ptp.gem.verificationPopupC") //$NON-NLS-1$
							|| id.equals("org.eclipse.ptp.gem.verificationPopupCpp") //$NON-NLS-1$
							|| id.equals("org.eclipse.ptp.gem.verificationPopupC++") //$NON-NLS-1$
							|| id.equals("org.eclipse.ptp.gem.verificationPopupCp") //$NON-NLS-1$
							|| id.equals("org.eclipse.ptp.gem.verificationPopupCc"); //$NON-NLS-1$

					// Save the URI of the most recent project resource
					// if (isValidSourceFile) {
					URI resourceLocation = GemUtilities.getRemoteLocationURI(resource);

					GemUtilities.saveMostRecentURI(resourceLocation);

					GemUtilities.initGemViews(resource, isValidSourceFile, true);

				} catch (final PartInitException e) {
					GemUtilities.logExceptionDetail(e);
				}
			} else {
				GemUtilities.showErrorDialog(Messages.VerificationPopUpAction_1);
				return;
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
