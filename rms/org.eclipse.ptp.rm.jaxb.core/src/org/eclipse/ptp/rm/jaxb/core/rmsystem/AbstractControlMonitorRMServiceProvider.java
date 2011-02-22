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

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider;
import org.eclipse.ptp.services.core.IServiceProvider;

public abstract class AbstractControlMonitorRMServiceProvider extends AbstractResourceManagerServiceProvider implements
		IControlMonitorRMConfiguration, IJAXBNonNLSConstants {

	private final List<String> controlInvocationOptions = new ArrayList<String>();
	private final List<String> monitorInvocationOptions = new ArrayList<String>();

	public AbstractControlMonitorRMServiceProvider() {
		// Empty
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public AbstractControlMonitorRMServiceProvider(IServiceProvider provider) {
		super(provider);
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

	/**
	 * Get the invocation options as a list of strings. Returns an empty list if
	 * there are no options
	 * 
	 * @return list of strings containing invocation options
	 */
	public List<String> getControlInvocationOptions() {
		addControlInvocationOptions(getString(TAG_CONTROL_INVOCATION_OPTIONS, ZEROSTR));
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
			String options = getString(TAG_CONTROL_OPTIONS, null);
			if (options != null) {
				return Integer.valueOf(options).intValue();
			}
			return 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getControlPath() {
		return getString(TAG_CONTROL_PATH, ZEROSTR);
	}

	/**
	 * Get the local address for the proxy to connect to
	 * 
	 * @return local address
	 */
	public String getLocalAddress() {
		return getString(TAG_LOCAL_ADDRESS, LOCALHOST);
	}

	public List<String> getMonitorInvocationOptions() {
		addMonitorInvocationOptions(getString(TAG_MONITOR_INVOCATION_OPTIONS, ZEROSTR));
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
			String options = getString(TAG_MONITOR_OPTIONS, null);
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
		return getString(TAG_MONITOR_PATH, ZEROSTR);
	}

	public void setConnectionName(String name, String type) {
		putString(type, name);
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
		putString(TAG_CONTROL_INVOCATION_OPTIONS, convertInvocationOptionsStr(getControlInvocationOptions()));
	}

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setControlOptions(int options) {
		putString(TAG_CONTROL_OPTIONS, String.valueOf(options));
	}

	/**
	 * Set the control path
	 * 
	 * @param path
	 */
	public void setControlPath(String path) {
		putString(TAG_CONTROL_PATH, path);

	}

	/**
	 * Set the local address
	 * 
	 * @param localAddr
	 */
	public void setLocalAddress(String localAddr) {
		putString(TAG_LOCAL_ADDRESS, localAddr);
	}

	public void setMonitorInvocationOptions(String optionString) {
		monitorInvocationOptions.clear();
		addMonitorInvocationOptions(optionString);
		putString(TAG_MONITOR_INVOCATION_OPTIONS, convertInvocationOptionsStr(getMonitorInvocationOptions()));
	}

	/**
	 * Set the remote configuration options
	 * 
	 * @param options
	 */
	public void setMonitorOptions(int options) {
		putString(TAG_MONITOR_OPTIONS, String.valueOf(options));
	}

	/**
	 * Set the monitor path
	 * 
	 * @param path
	 */
	public void setMonitorPath(String path) {
		putString(TAG_MONITOR_PATH, path);
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
