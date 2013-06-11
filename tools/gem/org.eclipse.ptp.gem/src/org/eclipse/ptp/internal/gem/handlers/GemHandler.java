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

package org.eclipse.ptp.internal.gem.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.gem.messages.Messages;
import org.eclipse.ptp.internal.gem.util.GemUtilities;
import org.eclipse.ptp.internal.gem.views.GemAnalyzer;
import org.eclipse.ptp.internal.gem.views.GemBrowser;
import org.eclipse.ptp.internal.gem.views.GemConsole;
import org.eclipse.ptp.rdt.core.resources.RemoteMakeNature;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for associated GEM commands declared in plugin.xml.
 * 
 * Extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GemHandler extends AbstractHandler {

	/**
	 * Constructor.
	 * 
	 * @param none
	 */
	public GemHandler() {
		super();
	}

	/**
	 * The command has been executed, so extract the needed information from the
	 * application context.
	 * 
	 * @param event
	 *            The Event to process
	 * @return Object <code>null</code>
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Process the command associated with the event
		final IWorkbench wb = PlatformUI.getWorkbench();
		final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		final Command cmd = event.getCommand();
		final String cmdString = cmd.getId();

		// If we are just changing number of processes, do so and break early
		if (cmdString.equals("org.eclipse.ptp.gem.commands.numprocsCommand")) { //$NON-NLS-1$
			GemUtilities.setNumProcesses();
			return null;
		}

		// Otherwise do analysis, filter extension and do the work
		final IEditorPart editor = page.getActiveEditor();
		IFile inputFile = null;
		boolean isSourceFileExtension = false;

		if (editor == null) {
			// do nothing here as there really is nothing to do
			return null;
		}

		final IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
		inputFile = editorInput.getFile();

		try {
			if (inputFile.getProject().hasNature(RemoteMakeNature.NATURE_ID)
					|| inputFile.getProject().hasNature(RemoteSyncNature.NATURE_ID)) {
				return null;
			}
		} catch (final CoreException ce) {
			GemUtilities.logExceptionDetail(ce);
		}

		final String extension = inputFile.getFileExtension();
		if (extension != null) {
			// The most common C & C++ source file extensions
			isSourceFileExtension = extension.equals("c") //$NON-NLS-1$
					|| extension.equals("cpp") || extension.equals("c++") //$NON-NLS-1$ //$NON-NLS-2$
					|| extension.equals("cc") || extension.equals("cp"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (isSourceFileExtension) {
			// Save most recent file reference to hidden preference as a URI
			GemUtilities.saveMostRecentURI(inputFile.getLocationURI());

			// ask for command line arguments
			GemUtilities.setCommandLineArgs();

			// Activate all inactive views and open up to the console
			try {
				final IViewReference[] activeViews = page.getViewReferences();
				boolean foundBrowser = false;
				boolean foundAnalyzer = false;
				for (final IViewReference ref : activeViews) {
					if (ref.getId().equals(GemBrowser.ID)) {
						foundBrowser = true;
					}
					if (ref.getId().equals(GemAnalyzer.ID)) {
						foundAnalyzer = true;
					}
				}
				// open the Browser view if it's not already
				if (!foundBrowser) {
					page.showView(GemBrowser.ID);
				}
				// open the Analyzer view if it's not already
				if (!foundAnalyzer) {
					page.showView(GemAnalyzer.ID);
				}
				page.showView(GemConsole.ID);
				GemUtilities.initGemViews(inputFile, true, true);
			} catch (final PartInitException e) {
				GemUtilities.logExceptionDetail(e);
			}

		} else {
			GemUtilities.showErrorDialog(Messages.GemHandler_1);
		}
		return null;
	}
}
