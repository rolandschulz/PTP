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
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.simulator.core.AbstractSimulatorDelegate;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.SimulatorException;
import org.eclipse.ptp.cell.simulator.core.SimulatorOperationException;
import org.eclipse.ptp.cell.simulator.tools.ContentRetrieverTool;
import org.eclipse.ptp.cell.utils.linux.commandline.ArgumentParser;


/**
 * An implementation of {@link IOperationDelegate} to launch the simulator locally.
 * @author Daniel Felix Ferber
 */
public class LocalSimulatorDelegate extends AbstractSimulatorDelegate {

	public LocalSimulatorDelegate(ISimulatorParameters configuration) {
		super(configuration);
	}
	
	public String[] createCellSimEnvironment() throws SimulatorException  {
		Map env = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
		
		addDefaultVariables(env);

		String[] strings = toVariablesArray(env);
		return strings;
	}




	public Process createGenericProcess(String workDirectory, String[] cmdarray, String[] environment) throws SimulatorException {
		Runtime runtime = Runtime.getRuntime();
		File workDir = null;
		if (workDirectory != null) {
			workDir = new File(workDirectory);
		}
		try {
			Process process = runtime.exec(cmdarray, environment, workDir);
			return process;
		} catch (IOException e) {
			throw new SimulatorException(NLS.bind(Messages.LocalSimulatorDelegate_CreatingProcessError, new String [] {new ArgumentParser(cmdarray).getCommandLine(true), e.getMessage()}), e);
		}
	}
	
	public Process createSimulatorProcess(String workDirectory, String[] cmdarray, String[] environment) throws  SimulatorException {
		ProcessFactory factory = ProcessFactory.getFactory();
		File workDir = null;
		if (workDirectory != null) {
			workDir = new File(workDirectory);
		}
		try {
			/*
			 * We still need to use the CDT process Spawner. The Process.terminate() of Java implementation
			 * tries to kill the simulator with sigterm. But the simulator will stop simulation instead of
			 * dieing on sigterm. The terminate() method from CDT Process uses SIGKILL instead.
			 */
			Process simulatorProcess = factory.exec(cmdarray, environment, workDir);				
			if (! (simulatorProcess instanceof Spawner)) {
				throw new SimulatorException(Messages.LocalSimulatorDelegate_SpawingPOSIXProcessProblem);
			}
			return simulatorProcess;
		} catch (IOException e) {
			throw new SimulatorException(NLS.bind(Messages.LocalSimulatorDelegate_CreatingProcessError, new String [] {new ArgumentParser(cmdarray).getCommandLine(true), e.getMessage()}), e);
		}
	}
	
	public void stopSimulatorProcess(Process process) {
		Spawner simulatorProcess = (Spawner) process;
		simulatorProcess.terminate();
	}
	
	public void destroySimulatorProcess(Process simulatorProcess) {
		simulatorProcess.destroy();
	}

	public void writeFile(String path, InputStream contentInputStrean) throws  SimulatorException {
		File file = new File(path);
		try {
			file.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(file);
			ContentRetrieverTool.copyStreamContent(outputStream, contentInputStrean);
			outputStream.close();
		} catch (Exception e) {
			throw new SimulatorException(NLS.bind(Messages.LocalSimulatorDelegate_CouldNotWrite, file.toString()), e);
		}
	}
	
	public void readFile(String path, OutputStream contentInputStrean) throws  SimulatorException {
		File file = new File(path);
		try {
			FileInputStream inputStream = new FileInputStream(file);
			ContentRetrieverTool.copyStreamContent(contentInputStrean, inputStream);
			inputStream.close();
		} catch (Exception e) {
			throw new SimulatorException(NLS.bind(Messages.LocalSimulatorDelegate_CouldNotRead, file.toString()), e);
		}
	}

	public void removeFile(String path) throws  SimulatorException {
		File file = new File(path);
		try {
			recursiveDeleteFile(file);
		} catch (Exception e) {
			throw new SimulatorException(NLS.bind(Messages.LocalSimulatorDelegate_CouldNotRemove, file.toString()));
		}
	}

	private static void recursiveDeleteFile(File f) {
		Stack s = new Stack();
		s.add(f);
		
		while (! s.empty()) {
			File file = (File) s.pop();
			if (file.exists()) {
				try {
					if (file.isDirectory()) {
						File [] l = file.listFiles();
						if (l.length == 0) {
							file.delete();
						} else {
							s.push(file);
							s.addAll(Arrays.asList(l));
						}
					} else {
						file.delete();
					}
				} finally {
					// Ignore
				}
			}
		}
	}

	public void logError(String string) {
		ILog log = CellSimulatorTargetPlugin.getDefault().getLog();
		org.eclipse.core.runtime.Status status = new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, string, null);
		log.log(status);
	}
	
	public void logError(String string, Exception e) {
		ILog log = CellSimulatorTargetPlugin.getDefault().getLog();
		org.eclipse.core.runtime.Status status = new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, string, e);
		log.log(status);		
	}

	public boolean fileExists(String logDirectory) {
		File file = new File(logDirectory);
		return (file.exists());
	}

	public void recursiveCreateDirectory(String path) throws Exception {
		File dir = new File(path);
		if (dir.exists()) {
			return;
		}
		if (! dir.mkdirs()) {
			throw new Exception(NLS.bind(Messages.LocalSimulatorDelegate_CreateLocalDirectoryFailed, path));
		}
	}
	
	public void verifyPath(int option, String path) throws Exception{
		File file = new File(path);

		if (! file.isAbsolute()) {
			throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_ABSOLUTE, path);
		}
		if (! file.exists()) {
			throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_EXIST, path);
		}
		if ((option & FILE) != 0) {
			if (! file.isFile()) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_FILE, path);
			}
		}
		if ((option & DIRECTORY) != 0) {
			if (! file.isDirectory()) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_DIRECTORY, path);
			}
		}
		if ((option & READ) != 0) {
			if (! file.canRead()) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_READABLE, path);
			}
		}
		if ((option & WRITE) != 0) {
			if (! file.canWrite()) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_WRITABLE, path);
			}
		}
	}

}
