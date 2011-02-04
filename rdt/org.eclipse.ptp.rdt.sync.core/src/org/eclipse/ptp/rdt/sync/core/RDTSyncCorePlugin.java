/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.sync.core.ResourceChangeListener;
import org.osgi.framework.BundleContext;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 * @since 2.0
 */
public class RDTSyncCorePlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$

	private static RDTSyncCorePlugin plugin = null;

	public RDTSyncCorePlugin() {
		plugin = this; // the platform will only instantiate once
	}

	public static RDTSyncCorePlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ResourceChangeListener.startListening();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			ResourceChangeListener.stopListening();
		} finally {
			super.stop(context);
		}
	}

	public static void log(String e) {
		log(createStatus(e));
	}

	public static void log(Throwable e) {
		log("Error", e); //$NON-NLS-1$
	}

	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}

	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, e);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
}
