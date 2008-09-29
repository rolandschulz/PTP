/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.launch.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchErrors;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.IRemoteScriptExecution;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;



/**
 * @author Ricardo M. Matinata
 * @since 1.2
 */
public class RemoteProcess extends Process {
	
	IRemoteConnection conn = null;
	String cmdLine;
	String id;
	IRemoteExecutionManager manager;
	IRemoteScriptExecution exec;
	boolean init;
	
	/**
	 * 
	 */
	public RemoteProcess(IRemoteConnection conn, String cmdLine, String id) throws CoreException {

		this.cmdLine = cmdLine;
		if (conn == null) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", null); //$NON-NLS-1$
			throw new CoreException(st);
		}
		this.conn = conn;
		init = true;
		this.manager = getExecutionManager();
		this.exec = startExecution(manager);
		
	}
	
	public RemoteProcess(IRemoteExecutionManager manager, String cmdLine, String id) throws CoreException {

		this.cmdLine = cmdLine;
		if (manager == null) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", null); //$NON-NLS-1$
			throw new CoreException(st);
		}
		init = true;
		this.manager = manager;
		this.exec = startExecution(manager);
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		
		if (init) {
			exec.cancel();
			exec.close();
			manager.cancel();
			init = false;
			manager = null;
			conn = null;
			
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		
		if (init) {
			if (!exec.wasFinished()) {
				throw new IllegalThreadStateException();
				
			}
		}
		
		if (exec != null) {
			return exec.getReturnCode();
		} else {
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		if (init) {
			try {
				return exec.getInputStreamFromProcessOutputStream();
			} catch (IOException e) {
				
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		if (init) {
			try {
				return exec.getOutputStreamToProcessInputStream();
			} catch (IOException e) {
				
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		
		if (init) {
			try {
				
				exec.waitForEndOfExecution();
				
				
			} catch (RemoteConnectionException e) {
				throw new InterruptedException();
			} catch (CancelException e) {
				throw new InterruptedException();
			}  catch (RemoteExecutionException e) {
				throw new InterruptedException();
			}
			
			destroy();
			
		}
		
		return exitValue();
	}
	
	private IRemoteExecutionManager getExecutionManager() throws CoreException {
		
		IRemoteExecutionManager manager = null;
		
		if (conn != null) {
			try {
				manager =  conn.createRemoteExecutionManager();
			} catch (RemoteConnectionException e) {
				init = false;
				IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", e); //$NON-NLS-1$
				throw new CoreException(st);
			}
		} else {
			manager = this.manager;
		}
		
		return manager;
		
	}

	private IRemoteScriptExecution startExecution(IRemoteExecutionManager manager) throws CoreException {
		
		IRemoteScriptExecution exec = null;
		if (manager != null) {
			try {
				manager = getExecutionManager();
				IRemoteScript script = manager.getExecutionTools().createScript();
				script.setScript(cmdLine);
				//script.setProcessOutputStream(System.out);
				exec = (IRemoteScriptExecution)manager.getExecutionTools().executeScript(script);
			} catch (RemoteConnectionException e) {
				init = false;
				IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", e); //$NON-NLS-1$
				throw new CoreException(st);
			} catch (RemoteExecutionException e) {
				init = false;
				IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_EXEC, "Error executing operation in target", e); //$NON-NLS-1$
				throw new CoreException(st);
			} catch (CancelException e) {
				init = false;
				IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CANCEL, "Canceled", e); //$NON-NLS-1$
				throw new CoreException(st);
			}
		}
		return exec;
		
	}
}
