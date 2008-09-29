/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.execution;

// No tracing required.

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.ui.console.IOConsole;

public class SPUTimingParameters {
	private String sputimingPath;
	private String inputFile;
	private File workingDirectory;
	private IOConsole console;
	private String parameters;

	/**
	 * @param sputimingPath
	 * @param inputFile
	 * @param workingDirectory
	 * @param console
	 */
	public SPUTimingParameters(String sputimingPath, String parameters,
			String inputFile, File workingDirectory, IOConsole console) {
		this.sputimingPath = sputimingPath;
		this.inputFile = inputFile;
		this.workingDirectory = workingDirectory;
		this.console = console;
		this.parameters = parameters;
	}

	public SPUTimingParameters() {

	}

	public String getInputFile() {
		return inputFile;
	}

	public String getSputimingPath() {
		return sputimingPath;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public IOConsole getConsole() {
		return console;
	}

	public void setConsole(IOConsole console) {
		this.console = console;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setSputimingPath(String sputimingPath) {
		this.sputimingPath = sputimingPath;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	public String toString() {
		return MessageFormat.format(Messages.SPUTimingParametersDumpString, 
				sputimingPath, 
				inputFile,
				workingDirectory.toString(), 
				parameters, 
				console.getName()
			);
	}
}
