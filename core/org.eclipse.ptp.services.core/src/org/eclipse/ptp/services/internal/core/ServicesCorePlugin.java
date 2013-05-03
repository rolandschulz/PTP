/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.internal.core;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.services.core.ProjectChangeListener;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.osgi.framework.BundleContext;


/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * The activator class controls the plug-in life cycle
 */
public class ServicesCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.services.core"; //$NON-NLS-1$

	// The shared instance
	private static ServicesCorePlugin plugin;

	/**
	 * The constructor
	 */
	public ServicesCorePlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		ProjectChangeListener.startListening();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			ProjectChangeListener.stopListening();
			try {
				ServiceModelManager.getInstance().saveModelConfiguration();
			} catch (Exception e) {
				log(e);
			}
		}
		finally {
			plugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ServicesCorePlugin getDefault() {
		return plugin;
	}

	public void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public void log(IStatus status) {
		getLog().log(status);
	}
	
	public void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

}
