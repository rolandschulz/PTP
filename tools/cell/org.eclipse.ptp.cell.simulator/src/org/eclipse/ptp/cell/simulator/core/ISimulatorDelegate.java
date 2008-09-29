/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.simulator.core;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.cell.simulator.internal.SimulatorControl;


/**
 * Provides architecture dependent implementations of methods required by
 * {@link SimulatorControl} to launch and control the simulator.
 * 
 * @author Daniel Felix Ferber
 */
public interface ISimulatorDelegate {
	/** 
	 * Create a process.
	 * 
	 * @param workDirectory The working directory
	 * @param cmdarray Array of string for the command line.
	 * @param environment Array of strings like key=value that define the environment.
	 * @return An implementation of {@link Process}.
	 * @throws SimulatorException If the process could not be created
	 */
	public abstract Process createSimulatorProcess(String workDirectory,
			String[] cmdarray, String[] environment) throws SimulatorException;
	public abstract void stopSimulatorProcess(Process simulatorProcess) throws SimulatorException;
	public abstract void destroySimulatorProcess(Process simulatorProcess);

	/** 
	 * Create a process.
	 * 
	 * @param workDirectory The working directory
	 * @param cmdarray Array of string for the command line.
	 * @param environment Array of strings like key=value that define the environment.
	 * @return An implementation of {@link Process}.
	 * @throws SimulatorException If the process could not be created
	 */
	public abstract Process createGenericProcess(String workDirectory,
			String[] cmdarray, String[] environment) throws SimulatorException;
	/**
	 * Write a file on path whose content is given by contentInputStrean.
	 * @param path Where to write the file
	 * @param contentInputStrean Where to get the file content
	 * @throws SimulatorException If file cannot be created/written
	 */
	public abstract void writeFile(String path, InputStream contentInputStrean)
			throws SimulatorException;

	/**
	 * Read a file on path and put its content into contentOutputStrean.
	 * @param path Where to read the file
	 * @param contentOutputStrean Where to put the file content
	 * @throws SimulatorException If file cannot be read
	 */
	public void readFile(String path, OutputStream contentOutputStrean)
			throws SimulatorException;

	/**
	 * Remove a file. If it is a directory, remove it recursively.
	 * @param path The file to remove.
	 * @throws SimulatorException If the file cannot be removed.
	 */
	public abstract void removeFile(String path) throws SimulatorException;

	/**
	 * Create an array of strings like key=value with the environment variables
	 * required to launch the simulator. The environment variables are used
	 * by the TCL script that is contributed by a profile extension point.
	 * @return The array with environment strings.
	 * @throws SimulatorException If the array cannot be calculated
	 */
	public abstract String[] createCellSimEnvironment()
			throws SimulatorException;

	/**
	 * Create an array of strings that contains the command line to launch the simulator.
	 * @return The array with the command line.
	 * @throws SimulatorException If the array cannot be calculated
	 */
	public abstract String[] createCommandLine() throws SimulatorException;

	/**
	 * Log an non serious error detected by {@link SimulatorControl}. Serios errors, instead,
	 * wil abort {@link SimulatorControl} with an exception.
	 * @param string The message to log.
	 */
	public abstract void logError(String string);

	/**
	 * Check if a file exists.
	 * @param path the file to check.
	 * @return True if the file exits, false otherwise.
	 * @throws SimulatorException 
	 */
	public abstract boolean fileExists(String path) throws SimulatorException;

	public abstract void logError(String string, Exception e);
	
	public abstract void recursiveCreateDirectory(String path) throws Exception;
	
	public static int EXIST = 0;
	public static int FILE = 1<<1;
	public static int DIRECTORY = 1<<2;
	public static int READ = 1<<3;
	public static int WRITE = 1<<4;

	public static int EXISTING_FILE = FILE;
	public static int EXISTING_DIR = DIRECTORY;
	public static int READABLE_FILE = EXISTING_FILE  | READ;
	public static int WRITABLE_FILE = EXISTING_FILE  | READ | WRITE;
	public static int ACCESSIBLE_DIR = DIRECTORY  | READ;
	
	public abstract void verifyPath(int options, String path) throws Exception;

	
}
