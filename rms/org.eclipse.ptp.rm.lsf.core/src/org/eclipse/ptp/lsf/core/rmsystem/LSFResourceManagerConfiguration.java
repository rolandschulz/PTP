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

import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class LSFResourceManagerConfiguration implements IResourceManagerConfiguration {
	
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_FACTORY_ID = "factoryId"; //$NON-NLS-1$
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_LAUNCH_MANUALLY = "launchManually"; //$NON-NLS-1$
	private static final String TAG_HOST = "host"; //$NON-NLS-1$

	public static IResourceManagerConfiguration load(LSFResourceManagerFactory factory,
			IMemento memento) {
		String factoryId = memento.getString(TAG_FACTORY_ID);
		if (!factoryId.equals(factory.getId())) {
			throw new IllegalStateException("Incompatable factory with factoryId"
					+ " stored id" + factoryId 
					+ ", factory id:" + factory.getId());
		}
		String name = memento.getString(TAG_NAME);
		String desc = memento.getString(TAG_DESCRIPTION);
		String serverFile = memento.getString(TAG_PROXY_PATH);
		String host = memento.getString(TAG_HOST);
		boolean launchManually = Boolean.parseBoolean(memento.getString(TAG_LAUNCH_MANUALLY));
		
		LSFResourceManagerConfiguration config = new LSFResourceManagerConfiguration(factory,
				name, desc, serverFile, host, launchManually);
		
		return config;
	}
	private String description;
	private String name;
	private final String factoryId;
	private String serverFile;

	private boolean launchManually;
	private String host;
	
	public LSFResourceManagerConfiguration(LSFResourceManagerFactory factory,
			String serverFile, String host, boolean launchManually) {
		this(factory, "", "", serverFile, host, launchManually);
		setDefaultNameAndDesc();
	}
	
	public LSFResourceManagerConfiguration(LSFResourceManagerFactory factory, String name, String desc,
			String serverFile, String host, boolean launchManually) {
		this.factoryId = factory.getId();
		this.name = name;
		this.description = desc;
		this.serverFile = serverFile;
		this.host = host;
		this.launchManually = launchManually;
	}

	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	public String getName() {
		return this.name;
	}

	public String getResourceManagerId() {
		return this.factoryId;
	}

	public String getServerFile() {
		return serverFile;
	}

	public boolean isLaunchManually() {
		return launchManually;
	}

	public void save(IMemento memento) {
		memento.putString(TAG_FACTORY_ID, getResourceManagerId());
		memento.putString(TAG_NAME, name);
		memento.putString(TAG_DESCRIPTION, description);
		memento.putString(TAG_PROXY_PATH, serverFile);
		memento.putString(TAG_HOST, host);
		memento.putString(TAG_LAUNCH_MANUALLY, Boolean.toString(launchManually));
	}

	public void setDefaultNameAndDesc() {
		this.name = "LSF";
		this.description = "LSF Resource Manager";
	}

	public void setDescription(String description) {
		this.description = description;
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

	public void setName(String name) {
		this.name = name;
	}

	public void setServerFile(String serverFile) {
		this.serverFile = serverFile;
	}
}