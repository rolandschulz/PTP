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
package org.eclipse.ptp.remotetools.environment;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ptp.remotetools.utils.extensionpoints.IProcessMemberVisitor;
import org.eclipse.ptp.remotetools.utils.extensionpoints.ProcessExtensions;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class EnvironmentPlugin extends Plugin {

	final public static String FILENAME = "environments.xml"; //$NON-NLS-1$
	final public static String EXT_CONTROLS_ID = "org.eclipse.ptp.remotetools.environment.core.remoteEnvironmentControlDelegate"; //$NON-NLS-1$
	final public static String EXT_PROVIDER_ID = "org.eclipse.ptp.remotetools.environment.core.remoteTargetProvider"; //$NON-NLS-1$
	final private static String ID = "org.eclipse.ptp.remotetools.environment.core"; //$NON-NLS-1$
	
	//The shared instance.
	private static EnvironmentPlugin plugin;
	private TargetEnvironmentManager manager;
	
	/**
	 * The constructor.
	 */
	public EnvironmentPlugin() {
		plugin = this;
	}
	
	public Map getControls() {
		final Map controls = new HashMap();
		ProcessExtensions.process(EXT_CONTROLS_ID, new IProcessMemberVisitor() {

			public Object process(IExtension extension, IConfigurationElement member) {
				Object mprovider;
				try {
					
					mprovider = member.createExecutableExtension("class"); //$NON-NLS-1$
					if ( ITargetTypeExtension.class.isAssignableFrom(mprovider.getClass()) ) {
						controls.put(member.getAttribute("name"),mprovider); //$NON-NLS-1$
					}
				} catch (CoreException e) {
					mprovider = null;
				}
				
				return mprovider;
			}
			
		});
		return controls;
	}
	
	public TargetEnvironmentManager getTargetsManager() {
		if (manager == null)
			manager = new TargetEnvironmentManager();
		return manager;
	}
	
	/*
	public ICellTargetProvider getCellTargetProvider() {
		
		ProcessExtensions.process(EXT_PROVIDER_ID, new IProcessMemberVisitor() {

			public Object process(IExtension extension, IConfigurationElement member) {
				Object mprovider;
				try {
					mprovider = member.createExecutableExtension("class");
					if ( !ICellTargetProvider.class.isAssignableFrom(mprovider.getClass()) ) {
						mprovider = null;
					}
				} catch (CoreException e) {
					mprovider = null;
				}
				provider = (ICellTargetProvider) mprovider;
				return mprovider;
			}
			
		});
		return provider;
	}
	*/
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		manager.writeToFile();
		plugin = null;
	}
	
	/**
	 * Notifies all target elements of the plugin that includes
	 * the parameter class. This parameter class must implement the
	 * ITargetTypeExtension interface.
	 * 
	 * @param Class The class that implements the ITargetTypeExtension interface
	 */
	public synchronized void destroyTypeElements(Class extensionClass) {
		// Find the TargetTypeElement that contains an extension which class is
		// equivalent to the argument.
		List typeList = getTargetsManager().getTypeElements();
		for(Iterator typeIt = typeList.iterator(); typeIt.hasNext();) {
			TargetTypeElement typeElement = (TargetTypeElement)typeIt.next();
			if(typeElement.getExtension().getClass().equals(extensionClass)) {
				// NOTE: At this point the called plugin should not be closed, so
				// its safe to play around with its values.
				
				// Call the destroy method of all elements of the given type
				List elemList = typeElement.getElements();
				for(Iterator elemIt = elemList.iterator(); elemIt.hasNext();) {
					TargetElement el = (TargetElement)elemIt.next();		
					try { // Errors could happen when disabling the environment. Just ignore.
						ITargetControl ctl = el.getControl();
						ctl.destroy();
					} catch (Throwable t) {	}
				}
			}
		}
	}
	

	/**
	 * Returns the shared instance.
	 */
	public static EnvironmentPlugin getDefault() {
		return plugin;
	}

	public static String getUniqueIdentifier() {
		
		return ID;
	}
	
	/*
	 * Unique ID generation for environment instances
	 * This ID is generation comes from the system's timestamp. 
	 */
	public String getEnvironmentUniqueID() {
		long envID = System.currentTimeMillis();
		return String.valueOf(envID);
	}
	
//	 Key that address the unique identifier of a given environment instance
	public static final String ATTR_CORE_ENVIRONMENTID = "core-environmentid";
	
}
