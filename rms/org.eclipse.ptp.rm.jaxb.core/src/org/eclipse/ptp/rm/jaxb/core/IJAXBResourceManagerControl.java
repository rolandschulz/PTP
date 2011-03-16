/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * Allows the JAXB Launch (Resource) Tab access to the Resource Manager's
 * internal data.
 * 
 * @author arossi
 * 
 */
public interface IJAXBResourceManagerControl extends IResourceManagerControl {

	boolean getAppendSysEnv();

	IJAXBResourceManagerConfiguration getConfig();

	Map<String, String> getDynSystemEnv();

	IJAXBResourceManagerConfiguration getJAXBRMConfiguration();

	IRemoteConnection getLocalConnection();

	IRemoteConnectionManager getLocalConnectionManager();

	IRemoteFileManager getLocalFileManager();

	IRemoteConnection getRemoteConnection();

	IRemoteConnectionManager getRemoteConnectionManager();

	IRemoteFileManager getRemoteFileManager();

	IRemoteServices getRemoteServices();

}
