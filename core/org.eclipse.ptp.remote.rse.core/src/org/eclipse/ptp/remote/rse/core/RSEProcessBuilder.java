/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.rse.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.rse.core.messages.Messages;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

public class RSEProcessBuilder extends AbstractRemoteProcessBuilder {
	private final static 	String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static 	String CMD_DELIMITER = ";"; //$NON-NLS-1$
	
	private final RSEConnection fConnection;
	private final RSEFileManager fFileMgr;
	
	private Map<String, String> fRemoteEnv = new HashMap<String, String>();

	public RSEProcessBuilder(IRemoteConnection conn, IRemoteFileManager fileMgr, List<String> command) {
		super(conn, command);
		fConnection = (RSEConnection)conn;
		fFileMgr = (RSEFileManager)fileMgr;
		fRemoteEnv = conn.getEnv();
	}

	public RSEProcessBuilder(IRemoteConnection conn, IRemoteFileManager fileMgr, String... command) {
		this(conn, fileMgr, Arrays.asList(command));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
//	@Override
//	public IFileStore directory() {
//		IFileStore dir = super.directory();
//		if (dir == null) {
//			dir = fFileMgr.getResource(connection().getWorkingDirectory());
//			directory(dir);
//		}
//		return dir;
//	}
	
	/**
	 * Convert environment map back to environment strings.
	 * 
	 * @return array of environment variables
	 */
	private String[] getEnvironment() {
		String[] env = new String[fRemoteEnv.size()];
		int pos = 0;
		for (Map.Entry<String, String> entry: fRemoteEnv.entrySet()) {
			env[pos++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
		}
		return env;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return fRemoteEnv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#start()
	 */
	public IRemoteProcess start() throws IOException {
		// The exit command is called to force the remote shell to close after our command 
		// is executed. This is to prevent a running process at the end of the debug session.
		// See Bug 158786.
		List<String> cmdArgs = command();
		if (cmdArgs.size() < 1) {
			throw new IndexOutOfBoundsException();
		}
		
		String remoteCmd = ""; //$NON-NLS-1$
		
		for (int i = 0; i < cmdArgs.size(); i++) {
			if (i > 0) {
				remoteCmd += " "; //$NON-NLS-1$
			}
			remoteCmd += spaceEscapify(cmdArgs.get(i));
		}
		
		remoteCmd += CMD_DELIMITER + EXIT_CMD;
		
		IShellService shellService = fConnection.getRemoteShellService();
		if (shellService == null) {
			throw new IOException(Messages.RSEProcessBuilder_0);
		}
		
		// This is necessary because runCommand does not actually run the command right now.
		IHostShell hostShell = null;
		try {
			String initialDir = ""; //$NON-NLS-1$
			if (directory() != null) {
				initialDir = directory().toURI().getPath();
			}
			hostShell = shellService.runCommand(initialDir, remoteCmd, getEnvironment(), new NullProgressMonitor());
		} catch (SystemMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return new RSEProcess(hostShell, redirectErrorStream());
	}
	
	private String spaceEscapify(String inputString) {
		if(inputString == null)
			return null;
		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		if(super.directory() == null) {
			// get CWD
			Map<String, String> envMap = environment();
			
			// check PWD first for UNIX systems
			String cwd = envMap.get("PWD");
			
			// if that didn't work, try %CD% for Windows systems
			if(cwd == null) {
				cwd = envMap.get("CD");
			}
			
			if(cwd != null) {
				URI uri=null;
				try {
					uri = new URI("rse", fConnection.getHost().getHostName(), cwd, null);
				} catch (URISyntaxException e) {
					RSEAdapterCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, RSEAdapterCorePlugin.PLUGIN_ID, e.getMessage()));
				}
				try {
					return EFS.getStore(uri);
				} catch (CoreException e) {
					RSEAdapterCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, RSEAdapterCorePlugin.PLUGIN_ID, e.getMessage()));
				}
			}
				
		}
		return super.directory();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#getHomeDirectory()
	 */
	public IFileStore getHomeDirectory() {
		// determine the home directory using environment variables
		Map<String, String> envMap = environment();
		
		// check HOME first for UNIX systems
		String homeDir = envMap.get("HOME");
		if(homeDir == null) {
			homeDir = ""; //$NON-NLS-1$
		}
		
		// if that didn't work, try %USERPROFILE% for Windows systems
		if(homeDir == null) {
			homeDir = envMap.get("USERPROFILE");
			IPath homePath = new Path(homeDir);
			homeDir = "/" + homePath.toString();
		}
		
		if(homeDir != null) {
			URI uri=null;
			try {
				uri = new URI("rse", fConnection.getHost().getHostName(), homeDir, null);
			} catch (URISyntaxException e) {
				RSEAdapterCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, RSEAdapterCorePlugin.PLUGIN_ID, e.getMessage()));
			}
			try {
				return EFS.getStore(uri);
			} catch (CoreException e) {
				RSEAdapterCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, RSEAdapterCorePlugin.PLUGIN_ID, e.getMessage()));
			}
		}
		
		return null;
	}

}