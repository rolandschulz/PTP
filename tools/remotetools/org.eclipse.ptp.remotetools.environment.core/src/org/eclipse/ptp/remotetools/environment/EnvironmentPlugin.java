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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ChildrenProviderManager;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.extension.IProcessMemberVisitor;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ptp.remotetools.environment.extension.ProcessExtensions;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class EnvironmentPlugin extends Plugin {

	public final static String FILENAME = "environments.xml"; //$NON-NLS-1$
	public final static String EXT_CONTROLS_ID = "org.eclipse.ptp.remotetools.environment.core.remoteEnvironmentControlDelegate"; //$NON-NLS-1$
	private final static String PLUGIN_ID = "org.eclipse.ptp.remotetools.environment.core"; //$NON-NLS-1$

	// The shared instance.
	private static EnvironmentPlugin plugin;
	private TargetEnvironmentManager manager;
	private ChildrenProviderManager childrenProviderMgr;

	// Key that address the unique identifier of a given environment instance
	public static final String ATTR_CORE_ENVIRONMENTID = "core-environmentid"; //$NON-NLS-1$

	/**
	 * Returns the shared instance.
	 */
	public static EnvironmentPlugin getDefault() {
		return plugin;
	}

	/**
	 * Get unique identifier for this plugin
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * The constructor.
	 */
	public EnvironmentPlugin() {
		plugin = this;
	}

	/**
	 * Notifies all target elements of the plugin that includes the parameter
	 * class. This parameter class must implement the ITargetTypeExtension
	 * interface.
	 * 
	 * @param Class
	 *            The class that implements the ITargetTypeExtension interface
	 */
	@SuppressWarnings("rawtypes")
	public synchronized void destroyTypeElements(Class extensionClass) {
		// Find the TargetTypeElement that contains an extension which class is
		// equivalent to the argument.
		List<TargetTypeElement> typeList = getTargetsManager().getTypeElements();
		for (TargetTypeElement typeElement : typeList) {
			if (typeElement.getExtension().getClass().equals(extensionClass)) {
				// NOTE: At this point the called plugin should not be closed,
				// so
				// its safe to play around with its values.

				// Call the destroy method of all elements of the given type
				List<ITargetElement> elemList = typeElement.getElements();
				for (ITargetElement el : elemList) {
					try { // Errors could happen when disabling the environment.
							// Just ignore.
						ITargetControl ctl = el.getControl();
						ctl.destroy();
					} catch (Throwable t) {
					}
				}
			}
		}
	}

	public ChildrenProviderManager getChildrenProviderManager() {
		if (childrenProviderMgr == null) {
			childrenProviderMgr = new ChildrenProviderManager();
		}
		return childrenProviderMgr;
	}

	public Map<String, ITargetTypeExtension> getControls() {
		final Map<String, ITargetTypeExtension> controls = new HashMap<String, ITargetTypeExtension>();
		ProcessExtensions.process(EXT_CONTROLS_ID, new IProcessMemberVisitor() {

			public Object process(IExtension extension, IConfigurationElement member) {
				Object mprovider;
				try {

					mprovider = member.createExecutableExtension("class"); //$NON-NLS-1$
					if (ITargetTypeExtension.class.isAssignableFrom(mprovider.getClass())) {
						controls.put(member.getAttribute("name"), (ITargetTypeExtension) mprovider); //$NON-NLS-1$
					}
				} catch (CoreException e) {
					mprovider = null;
				}

				return mprovider;
			}

		});
		return controls;
	}

	/*
	 * Unique ID generation for environment instances This ID is generation
	 * comes from the system's timestamp.
	 */
	public String getEnvironmentUniqueID() {
		long envID = System.currentTimeMillis();
		return String.valueOf(envID);
	}

	public TargetEnvironmentManager getTargetsManager() {
		if (manager == null) {
			manager = new TargetEnvironmentManager();
		}
		return manager;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		if (manager != null) {
			manager.writeToFile();
		}
		plugin = null;
	}

}
