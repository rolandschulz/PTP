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
package org.eclipse.ptp.cell.utils.linux.findutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.ptp.cell.utils.debug.Debug;
import org.eclipse.ptp.cell.utils.process.ProcessController;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class Find {

	protected static final String PROCESS_NAME = "find"; //$NON-NLS-1$

	protected static final String FIND = "find"; //$NON-NLS-1$

	protected static final String FIND_NAME_FLAG = "-name"; //$NON-NLS-1$

	protected static final String FIND_WHOLENAME_FLAG = "-wholename"; //$NON-NLS-1$

	protected static final long TIMEOUT = Long.MAX_VALUE;

	protected static final String WHITESPACE = " "; //$NON-NLS-1$

	public static final String EMPTY_PATH = ""; //$NON-NLS-1$

	/**
	 * 
	 */
	public Find() {
	}

	/**
	 * Returns the full path of the first occurence of a file with the given name in the file system tree.
	 * 
	 * @param startSearchDirectory the directory from which the search will start
	 * @param fileName the file name for which we are going to search the full path
	 * @return the full path or null if not found.
	 */
	public static String findFile(String startSearchDirectory, String fileName) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, startSearchDirectory, fileName);

		String outputLine = EMPTY_PATH;
		String command = FIND + WHITESPACE + startSearchDirectory + WHITESPACE
				+ FIND_NAME_FLAG + WHITESPACE + fileName;

		try {
			Debug.POLICY.trace(Debug.DEBUG_LINUX, command);
			Process process = ProcessFactory.getFactory().exec(command);
			// The process controller thread will destroy the process if it
			// blocks for more than TIMEOUT seconds or if the user cancel the
			// operation.
			ProcessController processController = new ProcessController(
					PROCESS_NAME, process, TIMEOUT);
			processController.start();

			BufferedReader processOutput = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			outputLine = processOutput.readLine();
			Debug.POLICY.trace(Debug.DEBUG_LINUX_MORE, outputLine);

			// find can return a value different from 0 even if it runs successfully (e.g. if it encounter problems to read any directory).
			// Because of that, we are not going to check find return value.

			processController.interrupt();
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_LINUX, e);
			Debug.POLICY.exit(Debug.DEBUG_LINUX, EMPTY_PATH);
			Debug.POLICY.logError(e, Messages.Find_FailedExecution, command);
			return EMPTY_PATH;
		}

		// The process suceeded and its output indicates the path was found or nothing was read (null)
		if (outputLine == null) {
			Debug.POLICY.exit(Debug.DEBUG_LINUX, EMPTY_PATH);
			return EMPTY_PATH;
		}
		Debug.POLICY.exit(Debug.DEBUG_LINUX, outputLine);
		return outputLine;

	}

	/**
	 * Returns the full path of a file whose path matches pathNamePattern if its directory is in the file system tree.
	 * 
	 * @param startSearchDirectory the directory from which the search will start
	 * @param pathNamePattern the file path pattern for which we are going to search the full path
	 * @return the full path or null if not found.
	 */
	public static String findWholename(String startSearchDirectory,
			String pathNamePattern) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_LINUX, startSearchDirectory, pathNamePattern);

		String outputLine = EMPTY_PATH;
		String command = FIND + WHITESPACE + startSearchDirectory + WHITESPACE
				+ FIND_WHOLENAME_FLAG + WHITESPACE + pathNamePattern;

		try {
			Debug.POLICY.trace(Debug.DEBUG_LINUX, command);
			Process process = ProcessFactory.getFactory().exec(command);
			// The process controller thread will destroy the process if it
			// blocks for more than TIMEOUT seconds or if the user cancel the
			// operation.
			ProcessController processController = new ProcessController(
					PROCESS_NAME, process, TIMEOUT);
			processController.start();

			BufferedReader processOutput = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			outputLine = processOutput.readLine();
			Debug.POLICY.trace(Debug.DEBUG_LINUX_MORE, outputLine);

			// find can return a value different from 0 even if it runs successfully (e.g. if it encounter problems to read any directory).
			// Because of that, we are not going to check find return value.

			processController.interrupt();
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_LINUX, e);
			Debug.POLICY.exit(Debug.DEBUG_LINUX, EMPTY_PATH);
			Debug.POLICY.logError(e, Messages.Find_FailedExecution, command);
			return EMPTY_PATH;
		}

		// The process succeeded and its output indicates the path was found or nothing was read (null)
		if (outputLine == null) {
			Debug.POLICY.exit(Debug.DEBUG_LINUX, EMPTY_PATH);
			return EMPTY_PATH;
		}
		Debug.POLICY.exit(Debug.DEBUG_LINUX, outputLine);
		return outputLine;

	}

}
