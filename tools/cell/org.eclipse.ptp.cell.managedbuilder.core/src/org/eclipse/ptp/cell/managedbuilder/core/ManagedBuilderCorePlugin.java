/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.core;

import java.util.Date;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;
import org.osgi.framework.BundleContext;


/**
 * @author laggarcia
 *
 */
public class ManagedBuilderCorePlugin extends Plugin {

	private static final String PLUGIN_ID = "org.eclipse.cdt.managedbuilder.core"; //$NON-NLS-1$

	// The shared instance
	private static ManagedBuilderCorePlugin instance;

	/**
	 * @param descriptor
	 */
	public ManagedBuilderCorePlugin() {
		super();
		instance = this;
	}

	public static String getUniqueIdentifier() {
		if (instance == null) {
			// If the default instance is not yet initialized,
			// return a static identifier.
			return PLUGIN_ID;
		}
		return instance.getBundle().getSymbolicName();
	}

	/**
	 * Returns the shared instance.
	 */
	public static ManagedBuilderCorePlugin getDefault() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle started at {0}", date.toString()); //$NON-NLS-1$
		}
	}
	
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle stopped at {0}", date.toString()); //$NON-NLS-1$
		}
	}

}
