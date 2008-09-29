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

public class CompilerParameters {
	private String compilerPath;
	private String compilerFlags;
	private String [] compilerEnvironment;
	private String sourceFile;
	private File workingDirectory;
	private IOConsole console;

	/**
	 * @param compilerPath
	 * @param compilerFlags
	 * @param sourceFile
	 * @param workingDirectory
	 * @param console
	 */
	public CompilerParameters(String compilerPath, String compilerFlags,
			String [] compilerEnvironment, String sourceFile, File workingDirectory, 
			IOConsole console) {
		this.compilerPath = compilerPath;
		this.compilerFlags = compilerFlags;
		this.compilerEnvironment = compilerEnvironment;
		this.sourceFile = sourceFile;
		this.workingDirectory = workingDirectory;
		this.console = console;
	}
	
	public CompilerParameters() {

	}

	public String getCompilerFlags() {
		return compilerFlags;
	}

	public String getCompilerPath() {
		return compilerPath;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public IOConsole getConsole() {
		return console;
	}

	public void setCompilerFlags(String compilerFlags) {
		this.compilerFlags = compilerFlags;
	}

	public void setCompilerName(String compilerPath) {
		this.compilerPath = compilerPath;
	}

	public void setConsole(IOConsole console) {
		this.console = console;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String toString() {
		return MessageFormat.format(Messages.CompilerParametersDumpString,
			compilerPath,
			compilerFlags,
			sourceFile,
			workingDirectory.toString(),
			console.getName()
			);
	}

	public String[] getCompilerEnvironment() {
		return compilerEnvironment;
	}

	public void setCompilerEnvironmentPath(String[] compilerEnvironment) {
		this.compilerEnvironment = compilerEnvironment;
	}
}
