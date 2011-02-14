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
package org.eclipse.ptp.rm.jaxb.core.rmsystem;

import java.util.List;

import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public interface IControlMonitorRMConfiguration extends IResourceManagerConfiguration {

	/**
	 * Append invocation options to existing options. The contents of
	 * optionString are split into space separated strings.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void addControlInvocationOptions(String optionString);

	/**
	 * Append invocation options to existing options. The contents of
	 * optionString are split into space separated strings.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void addMonitorInvocationOptions(String optionString);

	/**
	 * Get the invocation options as a list of strings. Returns an empty list if
	 * there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getControlInvocationOptions();

	/**
	 * Convert invocation options to a string
	 * 
	 * @return invocation options separated by spaces
	 */
	public String getControlInvocationOptionsStr();

	/**
	 * Get the proxy server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getControlPath();

	/**
	 * Get the local address for the proxy to connect to
	 * 
	 * @return local address
	 */
	public String getLocalAddress();

	/**
	 * Get the invocation options as a list of strings. Returns an empty list if
	 * there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getMonitorInvocationOptions();

	/**
	 * Convert invocation options to a string
	 * 
	 * @return invocation options separated by spaces
	 */
	public String getMonitorInvocationOptionsStr();

	/**
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getMonitorOptions();

	/**
	 * Get the proxy server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getMonitorPath();

	/**
	 * Set the invocation options. The contents of optionString are split into
	 * space separated strings. Any existing options are discarded.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void setControlInvocationOptions(String optionString);

	/**
	 * Set the proxy server path
	 * 
	 * @param proxyServerPath
	 */
	public void setControlPath(String proxyServerPath);

	/**
	 * Set the local address
	 * 
	 * @param localAddr
	 */
	public void setLocalAddress(String localAddr);

	/**
	 * Set the invocation options. The contents of optionString are split into
	 * space separated strings. Any existing options are discarded.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void setMonitorInvocationOptions(String optionString);

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setMonitorOptions(int options);

	/**
	 * Set the proxy server path
	 * 
	 * @param proxyServerPath
	 */
	public void setMonitorPath(String proxyServerPath);

	/**
	 * Test if option is set.
	 * 
	 * @param option
	 *            option to check
	 * @return true if option is set
	 */
	public boolean testOption(int option);

}