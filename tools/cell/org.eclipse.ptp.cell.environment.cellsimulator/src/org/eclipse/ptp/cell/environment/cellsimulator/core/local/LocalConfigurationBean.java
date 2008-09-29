/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.LocalDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigFactory;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigurationBean;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;


public class LocalConfigurationBean extends CommonConfigurationBean {
	
	private String pluginId = null;

	public static final String ATTR_PREFERENCES_PREFIX = "local-"; //$NON-NLS-1$
	
	public static final String AUTOMATIC_WORK_DIRECTORY = "automatic-work-directory"; //$NON-NLS-1$

	protected final static String [] SPECIFIC_KEY_ARRAY = { 
		AUTOMATIC_WORK_DIRECTORY
	};

	protected final static String [] SPECIFIC_KEY_CIPHERED_ARRAY = {
	};
	
	public final static String [] KEY_ARRAY;
	public final static String [] KEY_CIPHERED_ARRAY;
	
	static {
		List list;
		
		list = new Vector(Arrays.asList(CommonConfigurationBean.SPECIFIC_KEY_ARRAY));
		list.addAll(Arrays.asList(SPECIFIC_KEY_ARRAY));
		KEY_ARRAY= (String[]) list.toArray(new String [list.size()]);
		
		list = new Vector(Arrays.asList(CommonConfigurationBean.SPECIFIC_KEY_CIPHERED_ARRAY));
		list.addAll(Arrays.asList(SPECIFIC_KEY_CIPHERED_ARRAY));
		KEY_CIPHERED_ARRAY= (String[]) list.toArray(new String [list.size()]);
	}
	
	public LocalConfigurationBean() {
		super();
		createAutomaticConfiguration();
	}
	
	public LocalConfigurationBean(Map newMap, String pluginId) {
		super(newMap);
		createAutomaticConfiguration();
		this.pluginId = pluginId;
	}

	protected void createDefaultMap() {
		super.createDefaultMap();
		
		defaultMap.put(ATTR_WORK_DIRECTORY, LocalDefaultValues.WORK_DIRECTORY);
		defaultMap.put(AUTOMATIC_WORK_DIRECTORY, LocalDefaultValues.AUTOMATIC_WORK_DIRECTORY);

		defaultMap.put(ATTR_SHOW_SIMULATOR_GUI, LocalDefaultValues.SHOW_SIMULATOR_GUI);
		defaultMap.put(ATTR_CONSOLE_SHOW_LINUX, LocalDefaultValues.CONSOLE_SHOW_LINUX); 
		defaultMap.put(ATTR_CONSOLE_SHOW_SIMULATOR, LocalDefaultValues.CONSOLE_SHOW_SIMULATOR); 
		
		defaultMap.put(ATTR_AUTOMATIC_NETWORK, LocalDefaultValues.AUTOMATIC_NETWORK); 
		defaultMap.put(ATTR_IP_HOST, LocalDefaultValues.IP_HOST); 
		defaultMap.put(ATTR_IP_SIMULATOR, LocalDefaultValues.IP_SIMULATOR); 
		defaultMap.put(ATTR_MAC_SIMULATOR, LocalDefaultValues.MAC_SIMULATOR); 
 
		defaultMap.put(ATTR_AUTOMATIC_PORTCONFIG, LocalDefaultValues.AUTOMATIC_PORTCONFIG);
		defaultMap.put(ATTR_JAVA_API_SOCKET_PORT, LocalDefaultValues.JAVA_API_SOCKET_PORT); 
		defaultMap.put(ATTR_CONSOLE_SOCKET_PORT, LocalDefaultValues.CONSOLE_SOCKET_PORT); 
		
		defaultMap.put(ATTR_SYSTEM_WORKSPACE, LocalDefaultValues.SYSTEM_WORKSPACE);
	}

	protected void createCurrentMapFromPreferences() {
		super.createCurrentMapFromPreferences();
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		
		currentMap.put(ATTR_SHOW_SIMULATOR_GUI, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_SHOW_SIMULATOR_GUI));
		currentMap.put(ATTR_CONSOLE_SHOW_LINUX, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_CONSOLE_SHOW_LINUX));
		currentMap.put(ATTR_CONSOLE_SHOW_SIMULATOR, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_CONSOLE_SHOW_SIMULATOR));

		currentMap.put(ATTR_SIMULATOR_BASE_DIRECTORY, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_SIMULATOR_BASE_DIRECTORY));
		currentMap.put(ATTR_WORK_DIRECTORY, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_WORK_DIRECTORY));
		currentMap.put(ATTR_ROOT_IMAGE_PATH, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_ROOT_IMAGE_PATH));
		currentMap.put(ATTR_KERNEL_IMAGE_PATH, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_KERNEL_IMAGE_PATH));

		currentMap.put(ATTR_SYSTEM_WORKSPACE, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_SYSTEM_WORKSPACE));
	}
	
	protected void createAutomaticConfiguration() {
		if (currentMap.containsKey(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID)) {
			/*
			 * Already has a plugin id..
			 * There is no need to set default automatic values.
			 */
			return;
		}
		
		/*
		 * This is a workaround.
		 * Insert the identifier of the environment 
		 */
		String targetID = EnvironmentPlugin.getDefault().getEnvironmentUniqueID();
		currentMap.put(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID, targetID);

		/*
		 * Set automatic suggestion of values.
		 */
		LocalLaunchAutomaticAttributeGenerator generator = LocalLaunchAutomaticAttributeGenerator.getAutomaticAttributeGenerator();
		if (LocalDefaultValues.doAutomaticNetworkConfig()) {
			String ipSimulator = generator.getSimulatorAddress(targetID);
			String ipHost = generator.getHostAddress(targetID);
			String macSimulator = generator.getMacAddress(targetID);
			currentMap.put(ATTR_IP_HOST, ipHost); 
			currentMap.put(ATTR_IP_SIMULATOR, ipSimulator); 
			currentMap.put(ATTR_MAC_SIMULATOR, macSimulator);
		} else {
			currentMap.put(ATTR_IP_HOST, LocalDefaultValues.IP_HOST); 
			currentMap.put(ATTR_IP_SIMULATOR, LocalDefaultValues.IP_SIMULATOR); 
			currentMap.put(ATTR_MAC_SIMULATOR, LocalDefaultValues.MAC_SIMULATOR);
		}

		if (LocalDefaultValues.doAutomaticPortConfig()) {
			int javaApiPort = generator.getJavaAPIPort(targetID);
			int consolePort = generator.getAnotherConsolePort(targetID);
			currentMap.put(ATTR_JAVA_API_SOCKET_PORT, Integer.toString(javaApiPort)); 
			currentMap.put(ATTR_CONSOLE_SOCKET_PORT, Integer.toString(consolePort)); 
		} else {
			currentMap.put(ATTR_JAVA_API_SOCKET_PORT, LocalDefaultValues.JAVA_API_SOCKET_PORT); 
			currentMap.put(ATTR_CONSOLE_SOCKET_PORT, LocalDefaultValues.CONSOLE_SOCKET_PORT); 			
		}
		
		
	}

	public CommonConfigFactory createFactory() {
		return new LocalConfigFactory(this, pluginId);
	}

}
