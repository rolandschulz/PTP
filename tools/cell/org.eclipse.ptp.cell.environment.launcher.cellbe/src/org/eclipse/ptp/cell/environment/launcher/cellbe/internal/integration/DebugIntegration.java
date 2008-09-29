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
package org.eclipse.ptp.cell.environment.launcher.cellbe.internal.integration;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchConstants;
import org.eclipse.ptp.cell.debug.launch.LocalCellCDILaunchDelegate;
import org.eclipse.ptp.cell.environment.launcher.cellbe.RemoteTargetLauncherPlugin;
import org.eclipse.ptp.cell.environment.launcher.cellbe.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.ITargetLaunchAttributes;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteStatusTools;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.core.NullLaunchIntegration;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


public class DebugIntegration extends NullLaunchIntegration implements ILaunchIntegration {
	LocalCellCDILaunchDelegate debugDelegate;
	IRemoteExecutionManager manager;
	private String mode;
	private ILaunchConfiguration config;
	private ILaunch launch;
	private IProgressMonitor monitor;
	private boolean doRemote = false;
	
	public DebugIntegration(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) {
		debugDelegate = new LocalCellCDILaunchDelegate();
		this.config = config;
		this.mode = mode;
		this.launch = launch;
		this.monitor = monitor;
	}
	
	public void setExecutionManager(IRemoteExecutionManager manager) {
		this.manager = manager;
	}

	public void start() throws CoreException {
		Set ports = null;
		try {
			ports = manager.getRemoteStatusTools().getRemotePortsInUse(IRemoteStatusTools.PROTO_TCP);
		} catch (RemoteConnectionException e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteTargetLauncherPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause()));
		} catch (RemoteOperationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteTargetLauncherPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause()));
		} catch (CancelException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (ports!=null) {
			String gdbPort = config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT,"0"); //$NON-NLS-1$
			Debug.POLICY.trace(Debug.DEBUG_INTEGRATION, "Checking if GDB port {0} is available,,,", gdbPort); //$NON-NLS-1$
			if (ports.contains(new Integer(gdbPort))) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteTargetLauncherPlugin.getDefault().getBundle().getSymbolicName(), 0, Messages.getString("DebugIntegration.0"), null)); //$NON-NLS-1$
			}
		}
	}
	
	public String[] createLaunchScript(String applicationFullPath, String[] arguments) throws CoreException {
		
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		copy.setAttribute(ICellDebugLaunchConstants.TARGET_ENV_SELECTED, copy.getAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID, "")); //$NON-NLS-1$
		config = copy.doSave();
		String [] script = debugDelegate.getRemoteDebuggerCommand(config, applicationFullPath, arguments); 
		if (Debug.DEBUG_INTEGRATION) {
			Debug.POLICY.trace("Launch script with GDB:"); //$NON-NLS-1$
			for (int i = 0; i < script.length; i++) {
				String string = script[i];
				Debug.POLICY.trace(string);
			}
		}
		return script;
	}
	
	public boolean getDoLaunchApplication() throws CoreException {
		this.doRemote = debugDelegate.doRemoteLaunch(config);
		return this.doRemote;
	}
	
	public void finalizeLaunch() throws CoreException, CancelException {
		boolean portAvail = false;
		
		while (!portAvail && this.doRemote) {
			Set ports = null;
			try {
				ports = manager.getRemoteStatusTools().getRemotePortsInUse(IRemoteStatusTools.PROTO_TCP);
			} catch (RemoteConnectionException e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteTargetLauncherPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause()));
			} catch (RemoteOperationException e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteTargetLauncherPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause()));
			} catch (CancelException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			if (ports!=null) {
				String gdbPort = config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT,"0"); //$NON-NLS-1$
				if (ports.contains(new Integer(gdbPort))) {
					portAvail = true;
					Debug.POLICY.trace(Debug.DEBUG_INTEGRATION, "GDB is listening for connection,"); //$NON-NLS-1$
				}
			}
			if (! portAvail) {
				try {
					Debug.POLICY.trace(Debug.DEBUG_INTEGRATION, "GDB not yet is listening for connection, Wainting..."); //$NON-NLS-1$
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
		debugDelegate.launchIntegration(config, mode, launch, monitor);
	}
}
