/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.xml.sax.SAXException;

public class JAXBTestsPlugin extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.rm.jaxb.tests"; //$NON-NLS-1$

	// The shared instance
	private static JAXBTestsPlugin fPlugin;

	/**
	 * The constructor
	 */
	public JAXBTestsPlugin() {
		fPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
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
				Preferences.savePreferences(getUniqueIdentifier());
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			Preferences.savePreferences(getUniqueIdentifier());
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
	 * @return
	 */
	public static CoreException coreErrorException(String message) {
		return new CoreException(new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), message));
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @param t
	 * @return
	 */
	public static CoreException coreErrorException(String message, Throwable t) {
		return new CoreException(new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), message, t));
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static JAXBTestsPlugin getDefault() {
		return fPlugin;
	}

	public static JAXBTestsPlugin getfPlugin() {
		return fPlugin;
	}

	public static URL getResource(String resource) throws IOException {
		URL url = null;
		if (getDefault() != null) {
			Bundle bundle = getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(JAXBCoreConstants.PATH_SEP + resource), null);
		} else {
			URI uri = new File(resource).toURI();
			url = uri.toURL();
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

	public static URL getURL(String name) throws IOException {
		URL instance = JAXBTestsPlugin.getResource(name);
		if (instance == null) {
			File f = new File(name);
			if (f.exists() && f.isFile()) {
				URI uri = f.toURI();
				instance = uri.toURL();
			} else {
				throw new FileNotFoundException(name);
			}
		}
		return instance;
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
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	public static void validate(String xml) throws SAXException, IOException, URISyntaxException {
		JAXBInitializationUtils.validate(getURL(xml));
	}
}
