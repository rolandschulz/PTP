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

package org.eclipse.ptp.gem.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.util.GemUtilities;
import org.eclipse.ptp.gem.views.GemAnalyzer;
import org.eclipse.ptp.gem.views.GemConsole;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Handler for associated Analyzer command declared in plugin.xml.
 * 
 * Extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GemHandler extends AbstractHandler {

	/**
	 * Constructor.
	 */
	public GemHandler() {
		super();
	}

	/**
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Find the active file
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		String sourceFilePath = ""; //$NON-NLS-1$

		// Get the command string
		Command cmd = event.getCommand();
		String cmdString = cmd.getId();

		// If we are just changing numprocs then do so and break early (don't
		// bother with curr file)
		if (cmdString.equals("org.eclipse.ptp.gem.commands.numprocsCommand")) { //$NON-NLS-1$
			GemUtilities.setNumProcs();
			return null;
		}

		// Likewise, if we are just opening the console do so and break
		if (cmdString.equals("org.eclipse.ptp.gem.commands.consoleCommand")) { //$NON-NLS-1$
			try {
				page.showView(GemConsole.ID);
			} catch (PartInitException pie) {
				GemUtilities.showExceptionDialog(Messages.GemHandler_3, pie);
				GemUtilities.logError(Messages.GemHandler_4, pie);
			}
			return null;
		}

		// Otherwise do the prep and open the Analyzer View
		// If no file is open get it from the one step history, if that
		// is also null then break early.
		if (editor == null) {
			sourceFilePath = GemUtilities.getLastFile();
			if (sourceFilePath.equals("")) { //$NON-NLS-1$
				GemUtilities.showErrorDialog(Messages.GemHandler_6,
						Messages.GemHandler_7);
				return null;
			}
		} else {
			// Extract the path
			IEditorInput input = editor.getEditorInput();
			IPath path = ((FileEditorInput) input).getPath();
			sourceFilePath = path.toString();
		}

		// Now that we have a valid path, save it to the one step history
		GemUtilities.saveLastFile(sourceFilePath);

		// Filter extensions
		int dotIndex = sourceFilePath.lastIndexOf("."); //$NON-NLS-1$
		if (dotIndex == -1) {
			GemUtilities.showErrorDialog(Messages.GemHandler_9,
					Messages.GemHandler_10);
			return null;
		}
		String extension = sourceFilePath.substring(dotIndex);
		boolean isSourceFileExtension = extension.equals(".c") //$NON-NLS-1$
				|| extension.equals(".cpp") || extension.equals(".c++") //$NON-NLS-1$ //$NON-NLS-2$
				|| extension.equals(".cc") || extension.equals(".cp"); //$NON-NLS-1$ //$NON-NLS-2$
		if (isSourceFileExtension || extension.equals(".log")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// If it was a log then don't use the path to it, use the path to
			// the file it was created from.
			if (extension.equals(".log")) { //$NON-NLS-1$
				File logFile = new File(sourceFilePath);
				try {
					Scanner scanner = new Scanner(logFile);

					// skip first line
					scanner.nextLine();
					sourceFilePath = scanner.nextLine();
					int index = sourceFilePath.lastIndexOf(" "); //$NON-NLS-1$
					sourceFilePath = sourceFilePath.substring(0, index);
					index = sourceFilePath.lastIndexOf(" "); //$NON-NLS-1$
					sourceFilePath = sourceFilePath.substring(index + 1,
							sourceFilePath.length());
				} catch (FileNotFoundException e) {
				}
			}

			// Open the Console View
			try {
				page.showView(GemConsole.ID);
			} catch (PartInitException pie) {
				GemUtilities.showExceptionDialog(Messages.GemHandler_17, pie);
				GemUtilities.logError(Messages.GemHandler_18, pie);
			}

			String logFilePath = GemUtilities.getLogFile(sourceFilePath);

			// Open the Analyzer View
			try {
				page.showView(GemAnalyzer.ID);
				GemUtilities.activateAnalyzer(sourceFilePath, logFilePath,
						true, false);
			} catch (PartInitException pie) {
				GemUtilities.showExceptionDialog(Messages.GemHandler_19, pie);
				GemUtilities.logError(Messages.GemHandler_20, pie);
			}

		} else {
			GemUtilities.showErrorDialog(Messages.GemHandler_21,
					Messages.GemHandler_22);
		}
		return null;
	}

}
