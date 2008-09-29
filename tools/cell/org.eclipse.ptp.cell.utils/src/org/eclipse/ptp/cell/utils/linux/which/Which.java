/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.linux.which;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.ptp.cell.utils.debug.Debug;
import org.eclipse.ptp.cell.utils.process.ProcessController;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class Which {

	protected static final String PROCESS_NAME = "which"; //$NON-NLS-1$

	protected static final String WHICH = "which"; //$NON-NLS-1$

	protected static final long TIMEOUT = 100;

	protected static final String WHITESPACE = " "; //$NON-NLS-1$

	public static final String EMPTY_PATH = ""; //$NON-NLS-1$

	/**
	 * 
	 */
	public Which() {
	}

	/**
	 * Returns the full path of the executable if its directory is defined in the PATH environment variable.
	 * 
	 * @param fileName the executable file name for which we are going to search the full path
	 * @return the full path or EMPTY_PATH if not found.
	 */
	public static String which(String fileName) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, fileName);
		String outputLine = EMPTY_PATH;
		String command = WHICH + WHITESPACE + fileName;

		try {
			Debug.POLICY.trace(Debug.DEBUG_LINUX, command);
			Process process = Runtime.getRuntime().exec(command);

			// The process controller thread will destroy the process if it
			// blocks for more than TIMEOUT seconds or if the user cancel the
			// operation.
			ProcessController processController = new ProcessController(
					PROCESS_NAME, process, TIMEOUT);
			processController.start();

			BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			outputLine = processOutput.readLine();
			Debug.POLICY.trace(Debug.DEBUG_LINUX_MORE, outputLine);
			// If something is read from which output we are fine. We don't need to wait till the end of the process execution.

			processController.interrupt();
		} catch (IOException ioe) {
			Debug.POLICY.error(Debug.DEBUG_LINUX, ioe);
			Debug.POLICY.exit(Debug.DEBUG_LINUX, EMPTY_PATH);
			Debug.POLICY.logError(ioe, Messages.FailedExecution0, command);
			return EMPTY_PATH;
		}

		// The process suceeded and its output can indicate that nothing was available for reading (null) or the path was found
		if (outputLine == null) {
			Debug.POLICY.exit(Debug.DEBUG_LINUX, EMPTY_PATH);
			return EMPTY_PATH;
		}
		if (Debug.DEBUG_LINUX) {
			if (outputLine.equals(EMPTY_PATH)) {
				Debug.POLICY.exit(EMPTY_PATH);
			} else {
				Debug.POLICY.exit(outputLine);
			}
		}			
		return outputLine;

	}

}
