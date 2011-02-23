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
	 * Get the connection name. If there are separate connections for control
	 * and monitoring, the type distinguishes them.
	 * 
	 * @param type
	 * @return connection name
	 */
	public String getConnectionName(String type);

	/**
	 * get the control connection user name.
	 * 
	 * @return name
	 */
	public String getControlAddress();

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
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getControlOptions();

	/**
	 * Get the server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getControlPath();

	/**
	 * get the control connection user name.
	 * 
	 * @return name
	 */
	public String getControlUserName();

	/**
	 * Get the local address for the remote service to connect to
	 * 
	 * @return local address
	 */
	public String getLocalAddress();

	/**
	 * get the control connection user name.
	 * 
	 * @return name
	 */
	public String getMonitorAddress();

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
	 * Get the server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getMonitorPath();

	/**
	 * get the monitor connection user name.
	 * 
	 * @return name
	 */
	public String getMonitorUserName();

	/**
	 * Set the connection name. If there are separate connections for control
	 * and monitoring, the type distinguishes them.
	 * 
	 * @param name
	 * @param type
	 */
	public void setConnectionName(String name, String type);

	/**
	 * Set the user name for monitor
	 * 
	 * @param name
	 */
	public void setControlAddress(String name);

	/**
	 * Set the invocation options. The contents of optionString are split into
	 * space separated strings. Any existing options are discarded.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void setControlInvocationOptions(String optionString);

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setControlOptions(int options);

	/**
	 * Set the server path
	 * 
	 * @param ServerPath
	 */
	public void setControlPath(String path);

	/**
	 * Set the user name for control
	 * 
	 * @param name
	 */
	public void setControlUserName(String name);

	/**
	 * Set the local address
	 * 
	 * @param localAddr
	 */
	public void setLocalAddress(String localAddr);

	/**
	 * Set the user name for monitor
	 * 
	 * @param name
	 */
	public void setMonitorAddress(String name);

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
	 * Set the server path
	 * 
	 * @param ServerPath
	 */
	public void setMonitorPath(String path);

	/**
	 * Set the user name for monitor
	 * 
	 * @param name
	 */
	public void setMonitorUserName(String name);

	/**
	 * Test if option is set.
	 * 
	 * @param option
	 *            option to check
	 * @return true if option is set
	 */
	public boolean testOption(int option);

}