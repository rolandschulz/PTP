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
package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.simulator.core.AbstractSimulatorDelegate;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.SimulatorException;
import org.eclipse.ptp.cell.simulator.core.SimulatorOperationException;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.core.IRemoteDownloadExecution;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.IRemoteUploadExecution;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.ptp.utils.core.linux.ArgumentParser;


/**
 * An implementation of {@link IOperationDelegate} to launch the simulator locally.
 * @author Daniel Felix Ferber
 */
public class RemoteSimulatorDelegate extends AbstractSimulatorDelegate {
	IRemoteExecutionManager executionManager;
	RemoteProcess simulatorProcess;
	int simulatorPID;
	boolean hasSimulatorPID = false;
	
	public RemoteSimulatorDelegate(ISimulatorParameters configuration, IRemoteExecutionManager executionManager) {
		super(configuration);
		this.executionManager = executionManager;
	}
	
	public String[] createCellSimEnvironment() throws SimulatorException  {
		// RemoteTools already inherits existing environment variables.
		Map env = new HashMap();
		
		addDefaultVariables(env);

		String[] strings = toVariablesArray(env);
		return strings;
	}

	public Process createGenericProcess(String workDirectory, String[] cmdarray, String[] environment) throws SimulatorException {
		if (executionManager == null) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_TargetHostNotConnected);
		}
		
		/*
		 * Create script.
		 */
		IRemoteExecutionTools executionTools = null;
		IRemoteScript script = null;
		try {
			executionTools = executionManager.getExecutionTools();
			script = executionTools.createScript();
//			script.setFetchProcessErrorStream(true);
//			script.setFetchProcessInputStream(true);
//			script.setFetchProcessOutputStream(true);
//			script.setForwardX11(true);
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_TargetHostConnectionFailed, e);			
		}
		
		/*
		 * Set environment variables.
		 */
		if (environment != null) {
			script.addEnvironment(environment);
		}
		
		/*
		 * Set command line and working directory.
		 */
		String commandLine = new ArgumentParser(cmdarray).getCommandLine(true);
		script.setScript(new String[] {"cd "+workDirectory, commandLine}); //$NON-NLS-1$
		
		/*
		 * Wrap as a java.lang.Process object.
		 */
		try {
			Process process = executionTools.executeProcess(script);
			return process;
			
		} catch (RemoteException e) {
			throw new SimulatorException(NLS.bind(Messages.RemoteSimulatorDelegate_ErrorCreatingProcess, new String [] {commandLine, e.getMessage()}), e);
		} catch (CancelException e) {
			throw new SimulatorException(NLS.bind(Messages.RemoteSimulatorDelegate_ErrorCreatingProcess, new String [] {commandLine, e.getMessage()}), e);
		}
	}
	
	public Process createSimulatorProcess(String workDirectory, String[] cmdarray, String[] environment) throws SimulatorException {
		if (executionManager == null) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_TargetHostNotConnected);
		}
		
		/*
		 * Create script.
		 */
		IRemoteExecutionTools executionTools = null;
		IRemoteScript script = null;
		try {
			executionTools = executionManager.getExecutionTools();
			script = executionTools.createScript();
//			script.setFetchProcessErrorStream(true);
//			script.setFetchProcessInputStream(true);
//			script.setFetchProcessOutputStream(true);
			script.setForwardX11(true);
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_TargetHostConnectionFailed, e);			
		}
		
		/*
		 * Set environment variables.
		 */
		if (environment != null) {
			script.addEnvironment(environment);
		}
		
		/*
		 * Set command line and working directory.
		 */
		String commandLine = new ArgumentParser(cmdarray).getCommandLine(true);
		script.setScript(new String[] {"cd "+workDirectory, "export", commandLine}); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * Wrap as a java.lang.Process object.
		 */
		try {
			simulatorProcess = executionTools.executeProcess(script);
			hasSimulatorPID = false;
			
		} catch (RemoteException e) {
			throw new SimulatorException(NLS.bind(Messages.RemoteSimulatorDelegate_ErrorCreatingProcess, new String [] {commandLine, e.getMessage()}), e);
		} catch (CancelException e) {
			throw new SimulatorException(NLS.bind(Messages.RemoteSimulatorDelegate_ErrorCreatingProcess, new String [] {commandLine, e.getMessage()}), e);
		}

		return simulatorProcess;
	}
	
	public void stopSimulatorProcess(Process process) throws SimulatorException {
		if (executionManager == null) {
			throw new IllegalStateException(Messages.RemoteSimulatorDelegate_TargetHostNotConnected);
		}
		/*
		 * Kill the simulator with SIGTERM (this will actually stop, not kill).
		 */
		try {
			readSimulatorPID();	
			IRemoteExecutionTools executionTools = executionManager.getExecutionTools();
			int result = executionTools.executeWithExitValue("kill -TERM "+Integer.toString(simulatorPID)); //$NON-NLS-1$
			if (result != 0) {
				throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotSendSignalToSimulator);
			}
		} catch (SimulatorException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotReadSimulatorPID, e);			
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotSendSignalToSimulator, e);
		} catch (RemoteExecutionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotSendSignalToSimulator, e);
		} catch (CancelException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotSendSignalToSimulator, e);
		}		
	}

	private void readSimulatorPID() throws SimulatorException {
		if (! hasSimulatorPID) {
			String pidFile = getConfiguration().getPIDPath();
			
			/*
			 * Check if file exists on remote host.
			 */
			// For some unknown reason, this piece of code does not work when called
			// to kill the simulator in the cleanup() method.
//			try {
//				IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
//				if (! fileTools.hasFile(pidFile)) {
//					throw new SimulatorException("Cannot find file containing Cell Simulator PID.");
//				}
//			} catch (RemoteConnectionException e) {
//				throw new SimulatorException("Cannot find file containing Cell Simulator PID.", e);
//			} catch (RemoteExecutionException e) {
//				throw new SimulatorException("Cannot find file containing Cell Simulator PID.", e);
//			} catch (CancelException e) {
//				throw new SimulatorException("Cannot find file containing Cell Simulator PID.", e);
//			}
			
			/*
			 * Read and parse content from PID file.
			 */
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				IRemoteCopyTools copyTools = executionManager.getRemoteCopyTools();
				IRemoteDownloadExecution execution = copyTools.executeDownload(pidFile, stream);
				execution.waitForEndOfExecution();
				String content = new String(stream.toByteArray());
				simulatorPID = Integer.parseInt(content.trim());
				hasSimulatorPID = true;
			} catch (NumberFormatException e) {
				throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotReadSimulatorPIDInFile, e);
			} catch (RemoteConnectionException e) {
				throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotReadSimulatorPIDInFile, e);
			} catch (RemoteExecutionException e) {
				throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotReadSimulatorPIDInFile, e);
			} catch (CancelException e) {
				throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotReadSimulatorPIDInFile, e);
			}
		}
	}
	
	public void destroySimulatorProcess(Process simulatorProcess) {
//		simulatorProcess.destroy();
		if (executionManager == null) {
			throw new IllegalStateException(Messages.RemoteSimulatorDelegate_TargetHostNotConnected);
		}
		try {
			readSimulatorPID();
			IRemoteExecutionTools executionTools = executionManager.getExecutionTools();
			executionTools.executeWithExitValue("kill -9 "+Integer.toString(simulatorPID)); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
		} catch (RemoteExecutionException e) {
			e.printStackTrace();
		} catch (CancelException e) {
			e.printStackTrace();
		} catch (SimulatorException e) {
			e.printStackTrace();
		}	
	}

	public void writeFile(String path, InputStream contentInputStrean) throws  SimulatorException {
		try {
			IRemoteCopyTools copyTools = executionManager.getRemoteCopyTools();
			IRemoteUploadExecution execution = (IRemoteUploadExecution) copyTools.executeUpload(path, contentInputStrean);
			execution.waitForEndOfExecution();
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotUploadFile, e);
		} catch (RemoteExecutionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotUploadFile, e);
		} catch (CancelException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotUploadFile, e);
		}
	}
	
	public void readFile(String path, OutputStream contentOutputStream) throws  SimulatorException {
		try {
			IRemoteCopyTools copyTools = executionManager.getRemoteCopyTools();
			IRemoteDownloadExecution execution = (IRemoteDownloadExecution) copyTools.executeDownload(path, contentOutputStream);
			execution.waitForEndOfExecution();
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotDownloadFile, e);
		} catch (RemoteExecutionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotDownloadFile, e);
		} catch (CancelException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotDownloadFile, e);
		}
	}

	public void removeFile(String path) throws  SimulatorException {
		try {
			IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
			fileTools.removeFile(path);
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotDeletePath, e);
		} catch (RemoteOperationException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotDeletePath, e);
		} catch (CancelException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotDeletePath, e);
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

	public boolean fileExists(String logDirectory) throws SimulatorException {
		try {
			IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
			return fileTools.hasDirectory(logDirectory);
		} catch (RemoteConnectionException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotCheckPath, e);
		} catch (RemoteOperationException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotCheckPath, e);
		} catch (CancelException e) {
			throw new SimulatorException(Messages.RemoteSimulatorDelegate_CannotCheckPath, e);
		}
	}

	public void recursiveCreateDirectory(String path) throws Exception {
		try {
			IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
			fileTools.createDirectory(path);
		} catch (Exception e) {
			throw new Exception(NLS.bind(Messages.RemoteSimulatorDelegate_CreateRemoteDirFailed, path), e);
		}
	}

	public void verifyPath(int options, String path) throws Exception {
		Assert.isNotNull(executionManager);
		
		IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
		IRemotePathTools pathTools = executionManager.getRemotePathTools();

		if (! pathTools.isAbsolute(path)) {
			throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_ABSOLUTE, path);
		}
		
		IRemoteItem item;
		try {
			item = fileTools.getItem(path);
		} catch (RemoteOperationException e) {
			throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_EXIST, path);
		}
		
		if (! item.exists()) {
			throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_EXIST, path);			
		}
		
		if ((options & FILE) != 0) {
			if (! (item instanceof IRemoteFile)) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_FILE, path);
			}
		}
		if ((options & DIRECTORY) != 0) {
			if (! (item instanceof IRemoteDirectory)) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_DIRECTORY, path);
			}
		}
		if ((options & READ) != 0) {
			if (! item.isReadable()) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_READABLE, path);
			}
		}
		if ((options & WRITE) != 0) {
			if (! item.isWritable()) {
				throw new SimulatorOperationException(SimulatorOperationException.PATH_NOT_WRITABLE, path);
			}
		}
	}

}
