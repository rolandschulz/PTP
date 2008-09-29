/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.cell.debug.launch.CellDebugRemoteDebugger;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration;
import org.eclipse.ptp.utils.core.extensionpoints.IProcessMemberVisitor;
import org.eclipse.ptp.utils.core.extensionpoints.ProcessExtensions;
import org.osgi.framework.BundleContext;



/**
 * @author Ricardo M. Matinata
 * @since 1.2
 */
public class CellDebugPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.cell.debug.core"; //$NON-NLS-1$
	final public static String EXT_REMOTE_DGB_ID = "org.eclipse.ptp.cell.debug.core.CellRemoteDebugger"; //$NON-NLS-1$

	// The shared instance
	private static CellDebugPlugin plugin;
	/**
	 * 
	 */
	public CellDebugPlugin() {
		plugin = this;
	}

	private Map getRemoteDbgConfigs() {
		final Map configs = new HashMap();
		ProcessExtensions.process(EXT_REMOTE_DGB_ID, new IProcessMemberVisitor() {

			public Object process(IExtension extension, IConfigurationElement member) {
				Object mprovider;
				try {
					
					mprovider = member.createExecutableExtension("class"); //$NON-NLS-1$
					if ( ICellDebugLaunchRemoteDebugConfiguration.class.isAssignableFrom(mprovider.getClass()) ) {
						String name = member.getAttribute("name"); //$NON-NLS-1$
						String dbgId = member.getAttribute("debuggerId"); //$NON-NLS-1$
						CellDebugRemoteDebugger dbg = new CellDebugRemoteDebugger();
						dbg.setDebugConfig((ICellDebugLaunchRemoteDebugConfiguration)mprovider);
						dbg.setDebuggerId(dbgId);
						dbg.setName(name);
						configs.put(name,dbg);
					}
				} catch (CoreException e) {
					mprovider = null;
				}
				
				return mprovider;
			}
			
		});
		return configs;
	}
	
	public String[] getRemoteDbgNames() {
		
		Map configs = getRemoteDbgConfigs();
		List names = new ArrayList();
		Iterator i = configs.keySet().iterator();
		
		while (i.hasNext()) {
			String name = (String) i.next();
			names.add(name);
		}
		
		return (String[]) names.toArray(new String[]{});
	}
	
	public CellDebugRemoteDebugger getRemoteDbgConfigByName(String name) {
		Map configs = getRemoteDbgConfigs();
		return (CellDebugRemoteDebugger) configs.get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		this.savePluginPreferences();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CellDebugPlugin getDefault() {
		return plugin;
	}

	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}
}
