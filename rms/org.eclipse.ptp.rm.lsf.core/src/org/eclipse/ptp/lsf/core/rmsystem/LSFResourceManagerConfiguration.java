/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.lsf.core.rmsystem;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class LSFResourceManagerConfiguration 
	extends AbstractResourceManagerConfiguration {
	
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_LAUNCH_MANUALLY = "launchManually"; //$NON-NLS-1$
	private static final String TAG_HOST = "host"; //$NON-NLS-1$

	public static IResourceManagerConfiguration load(LSFResourceManagerFactory factory,
			IMemento memento) {

		CommonConfig commonConfig = loadCommon(factory, memento);

		String serverFile = memento.getString(TAG_PROXY_PATH);
		String host = memento.getString(TAG_HOST);
		boolean launchManually = Boolean.parseBoolean(memento.getString(TAG_LAUNCH_MANUALLY));
		
		LSFResourceManagerConfiguration config = new LSFResourceManagerConfiguration(factory,
				commonConfig, serverFile, host, launchManually);
		
		return config;
	}
	private String serverFile;

	private boolean launchManually;
	private String host;
	
	public LSFResourceManagerConfiguration(LSFResourceManagerFactory factory,
			CommonConfig commonConfig,
			String serverFile, String host, boolean launchManually) {
		super(commonConfig, factory);
		this.serverFile = serverFile;
		this.host = host;
		this.launchManually = launchManually;
	}
	
	public LSFResourceManagerConfiguration(LSFResourceManagerFactory factory,
			String serverFile, String host, boolean launchManually) {
		this(factory, new CommonConfig(), serverFile, host, launchManually);
		setDefaultNameAndDesc();
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	public String getServerFile() {
		return serverFile;
	}

	public boolean isLaunchManually() {
		return launchManually;
	}

	public void setDefaultNameAndDesc() {
		setName("LSF");
		setDescription("LSF Resource Manager");
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param launchManually the launchManually to set
	 */
	public void setLaunchManually(boolean launchManually) {
		this.launchManually = launchManually;
	}

	public void setManualLaunch(boolean launchManually) {
		this.launchManually = launchManually;
	}

	public void setServerFile(String serverFile) {
		this.serverFile = serverFile;
	}

	protected void doSave(IMemento memento) {
		memento.putString(TAG_PROXY_PATH, serverFile);
		memento.putString(TAG_HOST, host);
		memento.putString(TAG_LAUNCH_MANUALLY, Boolean.toString(launchManually));
	}
}