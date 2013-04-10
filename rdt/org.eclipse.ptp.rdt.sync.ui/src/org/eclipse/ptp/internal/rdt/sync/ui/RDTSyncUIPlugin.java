/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Roland Schulz, University of Tennessee
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.sync.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.sync.ui.handlers.CommonMissingConnectionHandler;
import org.eclipse.ptp.internal.rdt.sync.ui.handlers.CommonSyncExceptionHandler;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class RDTSyncUIPlugin extends Plugin {

	private static RDTSyncUIPlugin fInstance = null;

	public static final String PLUGIN_ID = "org.eclipse.ptp.rdt.sync.ui"; //$NON-NLS-1$
	public static final String SYNCHRONIZE_EXTENSION = "synchronizeParticipants"; //$NON-NLS-1$

	public RDTSyncUIPlugin() {
		fInstance = this;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ResourceChangeListener.startListening();
		SyncManager.setDefaultSyncExceptionHandler(new CommonSyncExceptionHandler(false, true));
		SyncManager.setDefaultMissingConnectionHandler(new CommonMissingConnectionHandler());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			ResourceChangeListener.stopListening();
		} finally {
			super.stop(context);
		}
	}

	public static RDTSyncUIPlugin getDefault() {
		return fInstance;
	}

	public static void log(Throwable e) {
		log("Error", e); //$NON-NLS-1$
	}

	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, message, null));
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}
}
