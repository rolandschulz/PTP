/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.activator;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * @author crecoskie
 *
 */
public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.ptp.rdt.core"; //$NON-NLS-1$
	
	
	private static Activator plugin = null;
	
	
	public Activator() {
		plugin = this; // the platform will only instantiate once
	}
	
	
	public static Activator getDefault() {
		return plugin;
	}
	

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// startup code here
	}
	
	
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			// shutdown code here
		}
		finally {
			super.stop(context);
		}
	}
}
