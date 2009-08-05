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
package org.eclipse.ptp.rm.remote.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;


public abstract class AbstractRemoteResourceManagerConfiguration 
	extends AbstractResourceManagerConfiguration 
	implements IRemoteResourceManagerConfiguration {
	
	/**
	 * Static class to hold remote configuration information
	 * 
	 * @author greg
	 *
	 */
	static public class RemoteConfig {
		private final CommonConfig commonConfig;
		private final String proxyPath;
		private final String localAddress;
		private final String invocationOptions;
		private final int options;

		public RemoteConfig() {
			this(new CommonConfig(),EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, IRemoteProxyOptions.NONE);
		}
		
		public RemoteConfig(CommonConfig config, String path, 
				    String localAddr, String invocationOptions, int options) {
			this.commonConfig = config;
			this.proxyPath = path;
			this.localAddress = localAddr;
			this.invocationOptions = invocationOptions;
			this.options = options;
		}
		
		public CommonConfig getCommonConfig() {
			return commonConfig;
		}
		
		public String getInvocationOptions() {
		    return invocationOptions;
		}

		public String getLocalAddress() {
			return localAddress;
		}		
		
		public int getOptions() {
			return options;
		}
		
		public String getProxyPath() {
			return proxyPath;
		}
	}
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_OPTIONS = "options"; //$NON-NLS-1$
	private static final String TAG_INVOCATION_OPTIONS = "invocationOptions"; //$NON-NLS-1$
	private static final String TAG_LOCAL_ADDRESS = "localAddress"; //$NON-NLS-1$

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
		
		String proxyServerPath = memento.getString(TAG_PROXY_PATH);
		String localAddress = memento.getString(TAG_LOCAL_ADDRESS);
		int options = Integer.parseInt(memento.getString(TAG_OPTIONS));
		String invocationOptions = memento.getString(TAG_INVOCATION_OPTIONS);

		RemoteConfig config = 
			new RemoteConfig(commonConfig, proxyServerPath,
					 localAddress, invocationOptions, options);

		return config;
	}
	
	private String proxyServerPath;
	private String localAddress;
	private List<String> invocationOptions;
	private int options;
	
	public AbstractRemoteResourceManagerConfiguration(RemoteConfig remoteConfig, 
			IResourceManagerFactory factory) {
		super(remoteConfig.getCommonConfig(), factory);
		this.proxyServerPath = remoteConfig.getProxyPath();
		this.localAddress = remoteConfig.getLocalAddress();
		this.options = remoteConfig.getOptions();
		this.invocationOptions = new ArrayList<String>();
		setInvocationOptions(remoteConfig.getInvocationOptions());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#addInvocationOptions(java.lang.String)
	 */
	public void addInvocationOptions(String optionString) {
		if (!optionString.equals(EMPTY_STRING)) {
			String[] options = optionString.split(" "); //$NON-NLS-1$
			
			for (String option : options) {
				this.invocationOptions.add(option);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#getInvocationOptions()
	 */
	public List<String> getInvocationOptions()
	{
	    return invocationOptions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#getInvocationOptionsStr()
	 */
	public String getInvocationOptionsStr() {
		String opts = EMPTY_STRING;
		for (int i = 0; i < invocationOptions.size(); i++) {
			if (i > 0) {
				opts += " "; //$NON-NLS-1$
			}
			opts += invocationOptions.get(i);
		}
		return opts;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#getLocalAddress()
	 */
	public String getLocalAddress() {
		return localAddress;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#getOptions()
	 */
	public int getOptions() {
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#getProxyServerPath()
	 */
	public String getProxyServerPath() {
		return proxyServerPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_PROXY_PATH, proxyServerPath);
		memento.putString(TAG_LOCAL_ADDRESS, localAddress);
		memento.putString(TAG_OPTIONS, Integer.toString(options));
		memento.putString(TAG_INVOCATION_OPTIONS, getInvocationOptionsStr());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#setInvocationOptions(java.lang.String)
	 */
	public void setInvocationOptions(String optionString) {
		this.invocationOptions.clear();
		addInvocationOptions(optionString);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#setLocalAddress(java.lang.String)
	 */
	public void setLocalAddress(String localAddr) {
		this.localAddress = localAddr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#setOptions(int)
	 */
	public void setOptions(int options) {
		this.options = options;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#setProxyServerPath(java.lang.String)
	 */
	public void setProxyServerPath(String proxyServerPath) {
		this.proxyServerPath = proxyServerPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#testOption(int)
	 */
	public boolean testOption(int option) {
		return (getOptions() & option) == option;
	}
}