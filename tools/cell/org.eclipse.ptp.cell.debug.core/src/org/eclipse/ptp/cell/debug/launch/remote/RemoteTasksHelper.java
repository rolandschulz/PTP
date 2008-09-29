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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchErrors;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteUploadExecution;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;


/**
 * 
 * @author Ricardom M. Matinata
 * @since 1.2
 */
public class RemoteTasksHelper {
	
	IRemoteConnection conn;
	IRemoteExecutionManager manager;
	String id;
	
	public RemoteTasksHelper(ITargetControl control, String id) {
		conn = control.getConnection();
		this.id = id;
		
	}
	
	private IRemoteExecutionManager getManager() throws CoreException {
		
		if (conn == null) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", null); //$NON-NLS-1$
			throw new CoreException(st);
		}
			
		if (manager == null) {
			try {
				manager = conn.createRemoteExecutionManager();
			} catch (RemoteConnectionException e) {
				IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", e); //$NON-NLS-1$
				throw new CoreException(st);
			}
		}
		return manager;
	}
	
	public void cleanup() throws CoreException {
		
		if (manager != null)
			manager.cancel();
		manager = null;
	}
	
	public void copyToHost(IPath local, IPath remote) throws CoreException {
		
		IRemoteUploadExecution cp;
		try {
			FileInputStream fInputStream = new FileInputStream(local.toFile());
			cp = getManager().getRemoteCopyTools().executeUpload(remote.toOSString(), fInputStream);
			cp.waitForEndOfExecution();
			cp.close();
		} catch (RemoteConnectionException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", e); //$NON-NLS-1$
			throw new CoreException(st);
		} catch (RemoteExecutionException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_EXEC, "Error executing operation in target", e); //$NON-NLS-1$
			throw new CoreException(st);
		} catch (CancelException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CANCEL, "Canceled", e); //$NON-NLS-1$
			throw new CoreException(st);
		} catch (FileNotFoundException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_LOCAL_FILENFOUND, "Local File Not Found", e); //$NON-NLS-1$
			throw new CoreException(st);
		}
		
	}
	
	public void executeCmd(String cmd) throws CoreException{
		
		try {
			
			IRemoteExecutionTools exec = getManager().getExecutionTools();
			exec.executeBashCommand(cmd);
			
		} catch (RemoteConnectionException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CONNECT, "Error connecting to target", e); //$NON-NLS-1$
			throw new CoreException(st);
		} catch (RemoteExecutionException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_EXEC, "Error executing operation in target", e); //$NON-NLS-1$
			throw new CoreException(st);
		} catch (CancelException e) {
			IStatus st = new Status(IStatus.ERROR, id, ICellDebugLaunchErrors.ERR_REMOTE_CANCEL, "Canceled", e); //$NON-NLS-1$
			throw new CoreException(st);
		}
	}
	
	public RemoteProcess createProcess(String cmdLine) throws CoreException {
		
		IRemoteExecutionManager manager = getManager();
		this.manager = null;
		return new RemoteProcess(manager,cmdLine,id);
	}

}
