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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IControlMonitorRMConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;

public abstract class AbstractControlMonitorRMServiceProvider extends AbstractResourceManagerConfiguration implements
		IControlMonitorRMConfiguration, IJAXBNonNLSConstants {

	private final List<String> controlInvocationOptions = new ArrayList<String>();
	private final List<String> monitorInvocationOptions = new ArrayList<String>();

	public AbstractControlMonitorRMServiceProvider() {
		// Empty
	}

	/**
	 * Append invocation options to existing options. The contents of
	 * optionString are split into space separated strings.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void addControlInvocationOptions(String optionString) {
		if (!optionString.equals(ZEROSTR)) {
			String[] options = optionString.split(SP);

			for (String option : options) {
				controlInvocationOptions.add(option);
			}
		}
	}

	public void addMonitorInvocationOptions(String optionString) {
		if (!optionString.equals(ZEROSTR)) {
			String[] options = optionString.split(SP);

			for (String option : options) {
				monitorInvocationOptions.add(option);
			}
		}
	}

	public String getConnectionName(String type) {
		return getString(type, ZEROSTR);
	}

	public String getControlAddress() {
		return getString(CONTROL_ADDRESS, ZEROSTR);
	}

	/**
	 * Get the invocation options as a list of strings. Returns an empty list if
	 * there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getControlInvocationOptions() {
		addControlInvocationOptions(getString(CONTROL_INVOCATION_OPTIONS, ZEROSTR));
		return controlInvocationOptions;
	}

	/**
	 * Get invocation options
	 * 
	 * @return invocation options separated by spaces
	 */
	public String getControlInvocationOptionsStr() {
		return convertInvocationOptionsStr(getControlInvocationOptions());
	}

	/**
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getControlOptions() {
		try {
			String options = getString(CONTROL_OPTIONS, null);
			if (options != null) {
				return Integer.valueOf(options).intValue();
			}
			return 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getControlPath() {
		return getString(CONTROL_PATH, ZEROSTR);
	}

	public String getControlUserName() {
		return getString(CONTROL_USER_NAME, ZEROSTR);
	}

	/**
	 * Get the local address for the proxy to connect to
	 * 
	 * @return local address
	 */
	public String getLocalAddress() {
		return getString(LOCAL_ADDRESS, LOCALHOST);
	}

	public String getMonitorAddress() {
		return getString(MONITOR_ADDRESS, ZEROSTR);
	}

	public List<String> getMonitorInvocationOptions() {
		addMonitorInvocationOptions(getString(MONITOR_INVOCATION_OPTIONS, ZEROSTR));
		return monitorInvocationOptions;
	}

	public String getMonitorInvocationOptionsStr() {
		return convertInvocationOptionsStr(getMonitorInvocationOptions());
	}

	/**
	 * Get the remote configuration options.
	 * 
	 * @return remote configuration options
	 */
	public int getMonitorOptions() {
		try {
			String options = getString(MONITOR_OPTIONS, null);
			if (options != null) {
				return Integer.valueOf(options).intValue();
			}
			return 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Get the proxy server path. This may be a path on a remote system.
	 * 
	 * @return path
	 */
	public String getMonitorPath() {
		return getString(MONITOR_PATH, ZEROSTR);
	}

	public String getMonitorUserName() {
		return getString(MONITOR_USER_NAME, ZEROSTR);
	}

	public void setConnectionName(String name, String type) {
		putString(type, name);
	}

	public void setControlAddress(String name) {
		putString(CONTROL_ADDRESS, name);
	}

	/**
	 * Set the invocation options. The contents of optionString are split into
	 * space separated strings. Any existing options are discarded.
	 * 
	 * @param optionString
	 *            string containing the space separated invocation options
	 */
	public void setControlInvocationOptions(String optionString) {
		controlInvocationOptions.clear();
		addControlInvocationOptions(optionString);
		putString(CONTROL_INVOCATION_OPTIONS, convertInvocationOptionsStr(getControlInvocationOptions()));
	}

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setControlOptions(int options) {
		putString(CONTROL_OPTIONS, String.valueOf(options));
	}

	/**
	 * Set the control path
	 * 
	 * @param path
	 */
	public void setControlPath(String path) {
		putString(CONTROL_PATH, path);
	}

	public void setControlUserName(String name) {
		putString(CONTROL_USER_NAME, name);
	}

	/**
	 * Set the local address
	 * 
	 * @param localAddr
	 */
	public void setLocalAddress(String localAddr) {
		putString(LOCAL_ADDRESS, localAddr);
	}

	public void setMonitorAddress(String name) {
		putString(MONITOR_ADDRESS, name);
	}

	public void setMonitorInvocationOptions(String optionString) {
		monitorInvocationOptions.clear();
		addMonitorInvocationOptions(optionString);
		putString(MONITOR_INVOCATION_OPTIONS, convertInvocationOptionsStr(getMonitorInvocationOptions()));
	}

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setMonitorOptions(int options) {
		putString(MONITOR_OPTIONS, String.valueOf(options));
	}

	/**
	 * Set the monitor path
	 * 
	 * @param path
	 */
	public void setMonitorPath(String path) {
		putString(MONITOR_PATH, path);
	}

	public void setMonitorUserName(String name) {
		putString(MONITOR_USER_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration#testOption
	 * (int)
	 */
	public boolean testOption(int option) {
		return (getMonitorOptions() & option) == option;
	}

	protected void clearRMData() {
		controlInvocationOptions.clear();
		monitorInvocationOptions.clear();
		putString(CONTROL_USER_NAME, ZEROSTR);
		putString(MONITOR_USER_NAME, ZEROSTR);
		putString(CONTROL_ADDRESS, ZEROSTR);
		putString(MONITOR_ADDRESS, ZEROSTR);
		putString(CONTROL_PATH, ZEROSTR);
		putString(MONITOR_PATH, ZEROSTR);
		putString(CONTROL_OPTIONS, ZEROSTR);
		putString(MONITOR_OPTIONS, ZEROSTR);
		putString(MONITOR_INVOCATION_OPTIONS, ZEROSTR);
		putString(CONTROL_INVOCATION_OPTIONS, ZEROSTR);
		putString(LOCAL_ADDRESS, ZEROSTR);
	}

	/**
	 * Convert invocation options to a string
	 * 
	 * @return invocation options separated by spaces
	 */
	private static String convertInvocationOptionsStr(List<String> options) {
		String opts = ZEROSTR;
		for (int i = 0; i < options.size(); i++) {
			if (i > 0) {
				opts += SP;
			}
			opts += options.get(i);
		}
		return opts;
	}
}
