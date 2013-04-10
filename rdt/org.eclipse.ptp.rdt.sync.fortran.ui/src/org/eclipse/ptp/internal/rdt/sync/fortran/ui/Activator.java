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

package org.eclipse.ptp.internal.rdt.sync.fortran.ui;

import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
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
public class Activator extends Plugin {

	private static Activator fInstance = null;

	public static final String PLUGIN_ID = "org.eclipse.ptp.internal.rdt.sync.fortran.ui"; //$NON-NLS-1$
	public static final String SYNCHRONIZE_EXTENSION = "synchronizeParticipants"; //$NON-NLS-1$

	public Activator() {
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
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
		} finally {
			super.stop(context);
		}
	}

	public static Activator getDefault() {
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
		log(new Status(IStatus.ERROR, getPluginId(), ICStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}
}
