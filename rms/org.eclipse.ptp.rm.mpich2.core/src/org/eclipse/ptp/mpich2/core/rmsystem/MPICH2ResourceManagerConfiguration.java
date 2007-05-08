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
package org.eclipse.ptp.mpich2.core.rmsystem;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class MPICH2ResourceManagerConfiguration extends AbstractResourceManagerConfiguration {
	
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_LAUNCH_MANUALLY = "launchManually"; //$NON-NLS-1$

	public static IResourceManagerConfiguration load(MPICH2ResourceManagerFactory factory,
			IMemento memento) {
		
		CommonConfig commonConfig = loadCommon(factory, memento);
		
		String serverFile = memento.getString(TAG_PROXY_PATH);
		boolean launchManually = Boolean.parseBoolean(memento.getString(TAG_LAUNCH_MANUALLY));
		
		MPICH2ResourceManagerConfiguration config = new MPICH2ResourceManagerConfiguration(factory,
				commonConfig, serverFile, launchManually);
		
		return config;
	}
	private String serverFile;

	private boolean launchManually;
	
	public MPICH2ResourceManagerConfiguration(MPICH2ResourceManagerFactory factory,
			CommonConfig commonConfig,
			String serverFile, boolean launchManually) {
		super(commonConfig, factory);
		this.serverFile = serverFile;
		this.launchManually = launchManually;
	}
	
	public MPICH2ResourceManagerConfiguration(MPICH2ResourceManagerFactory factory,
			String serverFile, boolean launchManually) {
		this(factory, new CommonConfig(), serverFile, launchManually);
		setDefaultNameAndDesc();
	}

	public String getServerFile() {
		return serverFile;
	}

	public boolean isLaunchManually() {
		return launchManually;
	}

	public void setDefaultNameAndDesc() {
		setName("MPICH2");
		setDescription("MPICH2 Resource Manager");
	}

	public void setManualLaunch(boolean launchManually) {
		this.launchManually = launchManually;
	}

	public void setServerFile(String serverFile) {
		this.serverFile = serverFile;
	}

	protected void doSave(IMemento memento) {
		memento.putString(TAG_PROXY_PATH, serverFile);
		memento.putString(TAG_LAUNCH_MANUALLY, Boolean.toString(launchManually));
	}
}