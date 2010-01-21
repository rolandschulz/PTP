/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.core.rmsystem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider;
import org.eclipse.ptp.services.core.IServiceProvider;

public abstract class AbstractRemoteResourceManagerServiceProvider extends AbstractResourceManagerServiceProvider implements IRemoteResourceManagerConfiguration
{
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_OPTIONS = "options"; //$NON-NLS-1$
	private static final String TAG_INVOCATION_OPTIONS = "invocationOptions"; //$NON-NLS-1$
	private static final String TAG_LOCAL_ADDRESS = "localAddress"; //$NON-NLS-1$

	private List<String> invocationOptions = new ArrayList<String>();
	
	public AbstractRemoteResourceManagerServiceProvider() {
		// Empty
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider provider we are making a copy from
	 */
	public AbstractRemoteResourceManagerServiceProvider(IServiceProvider provider) {
		super(provider);
	}

	/**
	 * Append invocation options to existing options. The contents of optionString are 
	 * split into space separated strings.
	 * 
	 * @param optionString string containing the space separated invocation options
	 */
	public void addInvocationOptions(String optionString) {
		if (!optionString.equals("")) { //$NON-NLS-1$
			String[] options = optionString.split(" "); //$NON-NLS-1$
			
			for (String option : options) {
				invocationOptions.add(option);
			}
		}
	}
	
	/**
	 * Get the invocation options as a list of strings. Returns
	 * an empty list if there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getInvocationOptions() {
		addInvocationOptions(getString(TAG_INVOCATION_OPTIONS, "")); //$NON-NLS-1$
	    return invocationOptions;
	}
	
	/**
	 * Convert invocation options to a string
	 * 
	 * @return invocation options separated by spaces
	 */
	public String getInvocationOptionsStr() {
		String opts = ""; //$NON-NLS-1$
		for (int i = 0; i < invocationOptions.size(); i++) {
			if (i > 0) {
				opts += " "; //$NON-NLS-1$
			}
			opts += invocationOptions.get(i);
		}
		return opts;
	}
	
	/**
	 * Get the local address for the proxy to connect to
	 * 
	 * @return local address
	 */
	public String getLocalAddress() {
		return getString(TAG_LOCAL_ADDRESS, "localhost"); //$NON-NLS-1$
	}
	
	/**
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getOptions() {
		try {
			String options = getString(TAG_OPTIONS, null);
			if (options != null) {
				return Integer.valueOf(options).intValue();
			}
			return 0;
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	
	/**
	 * Get the proxy server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getProxyServerPath() {
		return getString(TAG_PROXY_PATH, ""); //$NON-NLS-1$
	}
	
	/**
	 * Set the invocation options. The contents of optionString are split into space
	 * separated strings. Any existing options are discarded.
	 * 
	 * @param optionString string containing the space separated invocation options
	 */
	public void setInvocationOptions(String optionString) {
		invocationOptions.clear();
		addInvocationOptions(optionString);
		putString(TAG_INVOCATION_OPTIONS, getInvocationOptionsStr());
	}
	
	/**
	 * Set the local address
	 * 
	 * @param localAddr
	 */
	public void setLocalAddress(String localAddr) {
		putString(TAG_LOCAL_ADDRESS, localAddr);
	}

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setOptions(int options) {
		putString(TAG_OPTIONS, String.valueOf(options));
	}

	/**
	 * Set the proxy server path
	 * 
	 * @param proxyServerPath
	 */
	public void setProxyServerPath(String proxyServerPath) {
		putString(TAG_PROXY_PATH, proxyServerPath);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#testOption(int)
	 */
	public boolean testOption(int option) {
		return (getOptions() & option) == option;
	}
}
