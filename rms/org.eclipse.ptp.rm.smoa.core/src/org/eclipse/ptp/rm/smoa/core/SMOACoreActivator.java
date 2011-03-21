/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The plug-in activator.
 */
public class SMOACoreActivator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.ptp.rm.smoa.core"; //$NON-NLS-1$

	private static SMOACoreActivator plugin;

	public static SMOACoreActivator getDefault() {
		return plugin;
	}

	public SMOACoreActivator() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
