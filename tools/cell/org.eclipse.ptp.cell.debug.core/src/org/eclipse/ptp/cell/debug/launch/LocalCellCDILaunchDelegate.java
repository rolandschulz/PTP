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
package org.eclipse.ptp.cell.debug.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.LocalCDILaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.cell.debug.core.debug.Debug;
import org.eclipse.ptp.cell.utils.linux.commandline.ArgumentParser;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl.TargetSocket;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;



/**
 * @author Ricardo M. Matinata
 * @since 1.2
 *
 */
public class LocalCellCDILaunchDelegate extends LocalCDILaunchDelegate {
	
	public LocalCellCDILaunchDelegate() {
		Debug.read();
	}

	public static String ID = "org.eclipse.ptp.cell.debug.launch.remoteCellDebugDelegate"; //$NON-NLS-1$
    
    private String assembleCmdLine(List cmds) {
		
//		Iterator i = cmds.iterator();
//		StringBuffer buffer = new StringBuffer();
//		
//		while (i.hasNext()) {
//			buffer.append((String) i.next());
//			buffer.append(" "); //$NON-NLS-1$
//		}
//		
//		String result = buffer.toString();
//		return result;
    	
    	/*
    	 * Changed by dfferber
    	 */
		
    	ArgumentParser parser = new ArgumentParser(cmds);
    	String result = parser.getCommandLine(true);
    	Debug.POLICY.pass(Debug.DEBUG_DELEGATE, result);
    	return result;
	}
    
    // NEW DEBUGGER
    
    public void launchIntegration( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
    	Debug.POLICY.enter(Debug.DEBUG_DELEGATE, config, mode, launch);

    	ILaunchConfiguration copy = null;
		if (config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH, 0) > 0) {
			if ( !(config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_CAPABLE, 0) > 0) ) {
				IStatus st = new Status(IStatus.ERROR, ID, ICellDebugLaunchErrors.ERR_NOT_REMOTE_CAPABLE, DebugLaunchMessages.getString("LocalCellCDILaunchDelegate.0"), null); //$NON-NLS-1$
				throw new CoreException(st);
			}
			if (!config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID,"localDebugger").equals( //$NON-NLS-1$
					config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_DBGID,"remoteDebugger"))) { //$NON-NLS-1$
				IStatus st = new Status(IStatus.ERROR, ID, ICellDebugLaunchErrors.ERR_LOCAL_REMOTE_MISMATCH, DebugLaunchMessages.getString("LocalCellCDILaunchDelegate.1"), null); //$NON-NLS-1$
				throw new CoreException(st);
			}
		}
		
		copy = processLaunch(config);
		
		if ( !monitor.isCanceled() && mode.equals( ILaunchManager.DEBUG_MODE ) && copy != null )
			super.launch(copy, mode, launch, monitor);
		
    	Debug.POLICY.exit(Debug.DEBUG_DELEGATE);

	}

    public String[] getRemoteDebuggerCommand(ILaunchConfiguration config, String applicationFullPath, String[] arguments) throws CoreException {
    	
    	ArrayList command = new ArrayList(1 + arguments.length);
		command.add(applicationFullPath);
		command.addAll(Arrays.asList(arguments));
		String gdbBin = config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_BINARY,""); //$NON-NLS-1$
		String gdbPort = config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT,"0"); //$NON-NLS-1$
		String cmdString = gdbBin + " host:" + gdbPort + " " + assembleCmdLine(command); //$NON-NLS-1$ //$NON-NLS-2$
		Debug.POLICY.pass(Debug.DEBUG_DELEGATE, cmdString);
		return new String[] {cmdString};
    }
    
    public boolean doRemoteLaunch(ILaunchConfiguration config) throws CoreException {
    	
    	if (config.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP,false)) {
    		if (config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH, 0) == 1) {
    			Debug.POLICY.pass(Debug.DEBUG_DELEGATE, true);
    			return true;
    		}
    	}
		Debug.POLICY.pass(Debug.DEBUG_DELEGATE, false);
    	return false;
    }
    
    protected ILaunchConfiguration processLaunch( ILaunchConfiguration config) throws CoreException {
		Debug.POLICY.enter(Debug.DEBUG_DELEGATE, config);
		String id = config.getAttribute(ICellDebugLaunchConstants.TARGET_ENV_SELECTED,""); //$NON-NLS-1$
		TargetEnvironmentManager manager = EnvironmentPlugin.getDefault().getTargetsManager();
		ITargetControl control = manager.selectControl(id);
		String specPort = config.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT,"0"); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy modifCopy = null;
		if (portNumberIsValid(specPort)) {
			TargetSocket tsocket = control.createTargetSocket(Integer.parseInt(specPort));
			Debug.POLICY.enter(Debug.DEBUG_DELEGATE, "Created socket: {0}:{1}", tsocket.host, tsocket.port); //$NON-NLS-1$			
			modifCopy = config.getWorkingCopy();
			modifCopy.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, tsocket.host);
			modifCopy.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, Integer.toString(tsocket.port));
		} else {
			IStatus st = new Status(IStatus.ERROR, ID, ICellDebugLaunchErrors.ERR_PORT_MISMATCH, DebugLaunchMessages.getString("LocalCellCDILaunchDelegate.3"), null); //$NON-NLS-1$
			throw new CoreException(st);
		}
		
		ILaunchConfiguration result = modifCopy.doSave();
		Debug.POLICY.exit(Debug.DEBUG_DELEGATE, result);
		return result;
	}
    
    public static String getUniqueID() {
    	return ID;
    }
    
    private boolean portNumberIsValid( String portNumber ) {
		try {
			int port = Integer.parseInt( portNumber );
			return ( port > 0 && port <= 0xFFFF );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}
}
