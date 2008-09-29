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
package org.eclipse.ptp.cell.sputiming;

import java.util.Date;

import org.eclipse.ptp.cell.sputiming.debug.Debug;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class Activator extends AbstractUIPlugin
{

//	 The plug-in ID
//	public static final String PLUGIN_ID = "org.eclipse.ptp.cell.sputiming";
	public static final String OBSERVER_EXTENSION_ID = "org.eclipse.ptp.cell.sputiming.observerpoint"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle started at {0}", date.toString()); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle stopped at {0}", date.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
