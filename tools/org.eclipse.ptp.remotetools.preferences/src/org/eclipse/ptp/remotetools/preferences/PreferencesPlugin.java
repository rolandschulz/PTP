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
package org.eclipse.ptp.remotetools.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class PreferencesPlugin extends AbstractUIPlugin {

	private static String PLUGIN_ID = "org.eclipse.ptp.remotetools.preferences"; //$NON-NLS-1$

	// The shared instance.
	private static PreferencesPlugin fPlugin;

	private static final IScopeContext[] contexts = new IScopeContext[] { new DefaultScope(), new InstanceScope() };

	private static final int DEFAULT_CONTEXT = 0;
	private static final int INSTANCE_CONTEXT = 1;

	/**
	 * Returns the shared instance.
	 */
	public static PreferencesPlugin getDefault() {
		return fPlugin;
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 * @since 2.0
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
	public PreferencesPlugin() {
		fPlugin = this;
	}

	/**
	 * @since 2.0
	 */
	public void addListener(IPreferenceChangeListener listener) {
		contexts[DEFAULT_CONTEXT].getNode(getUniqueIdentifier()).addPreferenceChangeListener(listener);
		contexts[INSTANCE_CONTEXT].getNode(getUniqueIdentifier()).addPreferenceChangeListener(listener);
	}

	/**
	 * @since 2.0
	 */
	public void removeListener(IPreferenceChangeListener listener) {
		contexts[DEFAULT_CONTEXT].getNode(getUniqueIdentifier()).removePreferenceChangeListener(listener);
		contexts[INSTANCE_CONTEXT].getNode(getUniqueIdentifier()).removePreferenceChangeListener(listener);
	}

	/**
	 * This method is called upon plug-in activation
	 * 
	 * @since 2.0
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
		try {
			contexts[DEFAULT_CONTEXT].getNode(getUniqueIdentifier()).flush();
			contexts[INSTANCE_CONTEXT].getNode(getUniqueIdentifier()).flush();
		} finally {
			super.stop(context);
			fPlugin = null;
		}
	}
}
