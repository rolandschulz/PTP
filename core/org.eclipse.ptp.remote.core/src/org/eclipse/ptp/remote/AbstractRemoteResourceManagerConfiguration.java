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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;


public abstract class AbstractRemoteResourceManagerConfiguration extends AbstractResourceManagerConfiguration {
	
	/**
	 * Static class to hold remote configuration information
	 * 
	 * @author greg
	 *
	 */
	static public class RemoteConfig {
		private final CommonConfig commonConfig;
		private final String proxyPath;
		private final String remoteServicesId;
		private final String connectionName;
		private final String invocationOptions;
		private final int options;

		public RemoteConfig() {
			this(new CommonConfig(), "", "", "", "", IRemoteProxyOptions.PORT_FORWARDING);
		}
		
		public RemoteConfig(CommonConfig config, String remoteId, String conn, String path, 
				    String invocationOptions, int options) {
			this.commonConfig = config;
			this.proxyPath = path;
			this.remoteServicesId = remoteId;
			this.connectionName = conn;
			this.options = options;
			this.invocationOptions = invocationOptions;
		}
		
		public CommonConfig getCommonConfig() {
			return commonConfig;
		}
		
		public String getConnectionName() {
			return connectionName;
		}

		public int getOptions() {
			return options;
		}

		public String getProxyPath() {
			return proxyPath;
		}		
		
		public String getRemoteServicesId() {
			return remoteServicesId;
		}
		
		public String getInvocationOptions() {
		    return invocationOptions;
		}
	}
	
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_OPTIONS = "options"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$
	private static final String TAG_INVOCATION_OPTIONS = "invocationOptions"; //$NON-NLS-1$

	/**
	 * Load remote configuration from saved information
	 * 
	 * @param factory
	 * @param memento
	 * @return
	 */
	public static RemoteConfig loadRemote(IResourceManagerFactory factory,
			IMemento memento) {

		CommonConfig commonConfig = loadCommon(factory, memento);
		
		String remoteServicesId = memento.getString(TAG_REMOTE_SERVICES_ID);
		String connectionName = memento.getString(TAG_CONNECTION_NAME);
		String proxyServerPath = memento.getString(TAG_PROXY_PATH);
		int options = Integer.parseInt(memento.getString(TAG_OPTIONS));
		String invocationOptions = memento.getString(TAG_INVOCATION_OPTIONS);

		RemoteConfig config = 
			new RemoteConfig(commonConfig, remoteServicesId, connectionName, proxyServerPath,
					 invocationOptions, options);

		return config;
	}
	
	private String remoteServicesId;
	private String connectionName;
	private String proxyServerPath;
	private List<String> invocationOptions;
	private int options;
	
	public AbstractRemoteResourceManagerConfiguration(RemoteConfig remoteConfig, 
			IResourceManagerFactory factory) {
		super(remoteConfig.getCommonConfig(), factory);
		this.remoteServicesId = remoteConfig.getRemoteServicesId();
		this.connectionName = remoteConfig.getConnectionName();
		this.proxyServerPath = remoteConfig.getProxyPath();
		this.options = remoteConfig.getOptions();
		this.invocationOptions = new ArrayList<String>();
		this.setInvocationOptions(remoteConfig.getInvocationOptions());
	}
	
	/**
	 * Get the connection name. This is a string used by the remote subsystem to
	 * identify a particular connection.
	 * 
	 * @return connection name
	 */
	public String getConnectionName() {
		return connectionName;
	}
	
	/**
	 * Get the proxy server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getProxyServerPath() {
		return proxyServerPath;
	}
	
	/**
	 * Get the ID of the remote services subsystem.
	 * 
	 * @return
	 */
	public String getRemoteServicesId() {
		return remoteServicesId;
	}

	/**
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getOptions() {
		return options;
	}
	
	/**
	 * Get the invocation options as a list of strings. Returns
	 * an empty list if there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getInvocationOptions()
	{
	    return invocationOptions;
	}

	/**
	 * Set the connection name.
	 * 
	 * @param connectionName
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setOptions(int options) {
		this.options = options;
	}

	/**
	 * Set the proxy server path
	 * 
	 * @param proxyServerPath
	 */
	public void setProxyServerPath(String proxyServerPath) {
		this.proxyServerPath = proxyServerPath;
	}

	/**
	 * Set the remote services subsystem id
	 * @param id
	 */
	public void setRemoteServicesId(String id) {
		this.remoteServicesId = id;
	}
	
	/**
	 * Set the invocation options. The contents of optionString are split into space
	 * separated strings. Any existing options are discarded.
	 * 
	 * @param optionString string containing the space separated invocation options
	 */
	public void setInvocationOptions(String optionString) {
		this.invocationOptions.clear();
		addInvocationOptions(optionString);
	}
	
	
	/**
	 * Append invocation options to existing options. The contents of optionString are 
	 * split into space separated strings.
	 * 
	 * @param optionString string containing the space separated invocation options
	 */
	public void addInvocationOptions(String optionString) {
		String[] options = optionString.split(" ");
		
		for (String option : options) {
			this.invocationOptions.add(option);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration#doSave(org.eclipse.ui.IMemento)
	 */
	protected void doSave(IMemento memento) {
		memento.putString(TAG_REMOTE_SERVICES_ID, remoteServicesId);
		memento.putString(TAG_CONNECTION_NAME, connectionName);
		memento.putString(TAG_PROXY_PATH, proxyServerPath);
		memento.putString(TAG_OPTIONS, Integer.toString(options));
		String opts = "";
		for (int i = 0; i < invocationOptions.size(); i++) {
			if (i > 0) {
				opts += " ";
			}
			opts += invocationOptions.get(i);
		}
		memento.putString(TAG_INVOCATION_OPTIONS, opts);
	}
}