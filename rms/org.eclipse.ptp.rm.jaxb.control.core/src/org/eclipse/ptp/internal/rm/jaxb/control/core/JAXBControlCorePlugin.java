/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class JAXBControlCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.rm.jaxb.control.core"; //$NON-NLS-1$

	// The shared instance
	private static JAXBControlCorePlugin fPlugin;

	/**
	 * The constructor
	 */
	public JAXBControlCorePlugin() {
		fPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			public void doneSaving(ISaveContext saveContext) {
				// Nothing
			}

			public void prepareToSave(ISaveContext saveContext) throws CoreException {
				// Nothing
			}

			public void rollback(ISaveContext saveContext) {
				// Nothing
			}

			public void saving(ISaveContext saveContext) throws CoreException {
				// Nothing
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			ResourcesPlugin.getWorkspace().removeSaveParticipant(getUniqueIdentifier());
		} finally {
			super.stop(context);
			fPlugin = null;
		}
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @return exception
	 */
	public static CoreException coreErrorException(String message) {
		return new CoreException(new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), message));
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @param t
	 * @return exception
	 */
	public static CoreException coreErrorException(String message, Throwable t) {
		return new CoreException(new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), message, t));
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static JAXBControlCorePlugin getDefault() {
		return fPlugin;
	}

	public static URL getResource(String resource) throws IOException {
		URL url = null;
		if (getDefault() != null) {
			Bundle bundle = getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(JAXBControlConstants.PATH_SEP + resource), null);
		} else {
			url = new File(resource).toURI().toURL();
		}
		return url;
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, Messages.JAXBControlCorePlugin_Exception_InternalError,
				e));
	}
}
