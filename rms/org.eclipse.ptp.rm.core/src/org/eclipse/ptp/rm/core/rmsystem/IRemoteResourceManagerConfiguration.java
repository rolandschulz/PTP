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

import java.util.List;

import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;

public interface IRemoteResourceManagerConfiguration extends IResourceManagerComponentConfiguration {

	/**
	 * Append invocation options to existing options. The contents of
	 * optionString are split into space separated strings.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void addInvocationOptions(String optionString);

	/**
	 * Get the invocation options as a list of strings. Returns an empty list if
	 * there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getInvocationOptions();

	/**
	 * Convert invocation options to a string
	 * 
	 * @return invocation options separated by spaces
	 */
	public String getInvocationOptionsStr();

	/**
	 * Get the local address for the proxy to connect to
	 * 
	 * @return local address
	 */
	public String getLocalAddress();

	/**
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getOptions();

	/**
	 * Get the proxy server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getProxyServerPath();

	/**
	 * Get the default configuration flag
	 * 
	 * @return true if the default configuration should be used
	 * @since 3.0
	 */
	public boolean getUseDefault();

	/**
	 * Set the invocation options. The contents of optionString are split into
	 * space separated strings. Any existing options are discarded.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void setInvocationOptions(String optionString);

	/**
	 * Set the local address
	 * 
	 * @param localAddr
	 */
	public void setLocalAddress(String localAddr);

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setOptions(int options);

	/**
	 * Set the proxy server path
	 * 
	 * @param proxyServerPath
	 */
	public void setProxyServerPath(String proxyServerPath);

	/**
	 * Set the default configuration flag
	 * 
	 * @param flag
	 *            true if the default configuration should be used
	 * @since 3.0
	 */
	public void setUseDefault(boolean flag);

	/**
	 * Test if option is set.
	 * 
	 * @param option
	 *            option to check
	 * @return true if option is set
	 */
	public boolean testOption(int option);

}