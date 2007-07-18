/*******************************************************************************
 * Copyright (c) 2007 The Regents of the University of California. 
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
package org.eclipse.ptp.remote;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;


public abstract class AbstractRemoteResourceManagerConfiguration extends AbstractResourceManagerConfiguration {
	
	static public class RemoteConfig {
		private final CommonConfig commonConfig;
		private final String proxyPath;
		private final String remoteServicesId;
		private final String connectionName;
		private final boolean launchManually;

		public RemoteConfig() {
			this(new CommonConfig(), "", "", "", false);
		}
		
		public RemoteConfig(CommonConfig config, String remoteId, String conn, String path, boolean manual) {
			this.commonConfig = config;
			this.proxyPath = path;
			this.remoteServicesId = remoteId;
			this.connectionName = conn;
			this.launchManually = manual;
		}
		
		public CommonConfig getCommonConfig() {
			return commonConfig;
		}
		
		public String getConnectionName() {
			return connectionName;
		}

		public boolean getLaunchManually() {
			return launchManually;
		}

		public String getProxyPath() {
			return proxyPath;
		}		
		
		public String getRemoteServicesId() {
			return remoteServicesId;
		}
	}
	
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_LAUNCH_MANUALLY = "launchManually"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$

	public static RemoteConfig loadRemote(IResourceManagerFactory factory,
			IMemento memento) {

		CommonConfig commonConfig = loadCommon(factory, memento);
		
		String remoteServicesId = memento.getString(TAG_REMOTE_SERVICES_ID);
		String connectionName = memento.getString(TAG_CONNECTION_NAME);
		String proxyServerPath = memento.getString(TAG_PROXY_PATH);
		boolean launchManually = Boolean.parseBoolean(memento.getString(TAG_LAUNCH_MANUALLY));

		RemoteConfig config = 
			new RemoteConfig(commonConfig, remoteServicesId, connectionName, proxyServerPath,
					launchManually);

		return config;
	}
	
	private String remoteServicesId;
	private String connectionName;
	private String proxyServerPath;
	private boolean launchManually;
	
	public AbstractRemoteResourceManagerConfiguration(RemoteConfig remoteConfig, 
			IResourceManagerFactory factory) {
		super(remoteConfig.getCommonConfig(), factory);
		this.remoteServicesId = remoteConfig.getRemoteServicesId();
		this.connectionName = remoteConfig.getConnectionName();
		this.proxyServerPath = remoteConfig.getProxyPath();
		this.launchManually = remoteConfig.getLaunchManually();
	}
	
	public String getConnectionName() {
		return connectionName;
	}
	
	public String getProxyServerPath() {
		return proxyServerPath;
	}
	
	public String getRemoteServicesId() {
		return remoteServicesId;
	}

	public boolean isLaunchManually() {
		return launchManually;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public void setDefaultNameAndDesc() {
		String name = "ORTE";
		if (connectionName != null && !connectionName.equals("")) {
			name += "@" + connectionName;
		}
		setName(name);
		setDescription("ORTE Resource Manager");
	}

	public void setManualLaunch(boolean launchManually) {
		this.launchManually = launchManually;
	}

	public void setProxyServerPath(String proxyServerPath) {
		this.proxyServerPath = proxyServerPath;
	}

	public void setRemoteServicesId(String id) {
		this.remoteServicesId = id;
	}

	protected void doSave(IMemento memento) {
		memento.putString(TAG_REMOTE_SERVICES_ID, remoteServicesId);
		memento.putString(TAG_CONNECTION_NAME, connectionName);
		memento.putString(TAG_PROXY_PATH, proxyServerPath);
		memento.putString(TAG_LAUNCH_MANUALLY, Boolean.toString(launchManually));
	}
}