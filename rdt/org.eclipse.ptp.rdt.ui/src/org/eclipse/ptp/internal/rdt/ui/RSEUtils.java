/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui;

import java.net.URI;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;


public class RSEUtils {
	
	private static String DEFAULT_CONFIG_DIR_NAME = ".eclipsesettings"; //$NON-NLS-1$
	
	private RSEUtils() {}
	

	/**
	 * Return the best RSE connection object matching the given host name.
	 * Attempts to return a connection object where the remote file subsystem
	 * is connected.
	 */
	public static IHost getConnection(String hostName) {
		if(hostName == null)
			return null;

		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IHost[] connections = sr.getHosts();

		for(IHost con : connections) {
			if (hostName.equalsIgnoreCase(con.getHostName())) {
				IRemoteFileSubSystem fss = getRemoteFileSubSystem(con);
				if (fss != null && fss.isConnected()) {
					return con;
				}
			}	
		}
		
		return null;
	}
	

	public static IHost getConnection(URI uri) {
		if(!"rse".equals(uri.getScheme())) //$NON-NLS-1$
			return null;
		
		String hostName = uri.getHost();
		return getConnection(hostName);
	}
	
	
	public static IFileServiceSubSystem getFileServiceSubSystem(IHost host) {
		IRemoteFileSubSystem[] fileSubsystems = RemoteFileUtility.getFileSubSystems(host);
		for(IRemoteFileSubSystem subsystem : fileSubsystems) {
			if(subsystem instanceof IFileServiceSubSystem && subsystem.isConnected()) {
				return (IFileServiceSubSystem) subsystem;
			}
		}
		return null;
	}
	
	public static IRemoteFileSubSystem getRemoteFileSubSystem(IHost host) {
		IRemoteFileSubSystem[] fileSubsystems = RemoteFileUtility.getFileSubSystems(host);
		for(IRemoteFileSubSystem subsystem : fileSubsystems) {
			if(subsystem != null && subsystem.isConnected()) {
				return subsystem;
			}
		}
		return null;
	}
	
	
	private static IHostFile getUserHome(IHost host) {
		IFileServiceSubSystem fileSubsystem = getFileServiceSubSystem(host);
		return fileSubsystem == null ? null :  fileSubsystem.getFileService().getUserHome();
	}
	
	
	public static String getUserHomeDirectory(IHost host) {
		IHostFile userHome = getUserHome(host);
		return userHome == null ? null : userHome.getAbsolutePath();
	}
	
	public static String getDefaultConfigDirectory(IHost host) {
		IFileServiceSubSystem fileSubsystem = getFileServiceSubSystem(host);
		try {
			fileSubsystem.connect(false, null);
		} catch (Exception e) {
			return null;
		}
		if(fileSubsystem != null) {
			IHostFile userHome = fileSubsystem.getFileService().getUserHome();
			if(userHome != null) {
				return userHome.getAbsolutePath() + fileSubsystem.getSeparator() + DEFAULT_CONFIG_DIR_NAME;
			}
		}
		
		return null;
	}
	
	
	
	
	
	public enum VerifyResult { VERIFIED, INVALID, ERROR }
	
	public static VerifyResult verifyRemoteConfigDirectory(IHost host, String path) {
		IRemoteFileSubSystem fileSubsystem = getRemoteFileSubSystem(host);
		if(fileSubsystem == null) 
			return VerifyResult.ERROR;
		
		try {
			IRemoteFile dir = fileSubsystem.getRemoteFileObject(path, new NullProgressMonitor());
			if(dir != null && dir.isDirectory() && dir.canWrite())
				return VerifyResult.VERIFIED;
			else
				return VerifyResult.INVALID;
			
		} catch (SystemMessageException e) {
			return VerifyResult.ERROR;
		}
		
		
	}
}
