/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rm.lml.core.messages.Messages;
import org.eclipse.ptp.rm.lml.core.util.DebugUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.xml.sax.SAXException;

public class LMLCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.rm.lml.core"; //$NON-NLS-1$

	// The shared instance.
	private static LMLCorePlugin fPlugin;

	/*
	 * Unmarshaller for JAXB-generator
	 */
	private static Unmarshaller unmarshaller;
	
	
	/*
	 * Marshaller for JAXB-model
	 */
	private static Marshaller marshaller;

	/*
	 * DateFormat for (EEE MMM d HH:mm:ss yyyy)
	 */
	private SimpleDateFormat simpleDateFormat1;

	/*
	 * DateFormat for (EEE MMM dd HH:mm:ss yyyy)
	 */
	private SimpleDateFormat simpleDateFormat2;

	/**
	 * Returns the shared instance.
	 */
	public static LMLCorePlugin getDefault() {
		return fPlugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = LMLCorePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
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
		if (DebugUtil.RM_TRACING) {
			System.err.println(msg);
		}
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, Messages.LMLCorePlugin_0, e));
	}

	/*
	 * Resource bundle
	 */
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public LMLCorePlugin() {
		super();

		fPlugin = this;

		try {
			resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".ParallelPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Getting the corresponding SimpleDateFormat to a given length:
	 * 
	 * @param length
	 *            of a string
	 * @return
	 */
	public SimpleDateFormat getSimpleDateFormat(int length) {
		return length == simpleDateFormat1.toPattern().length() ? simpleDateFormat1 : simpleDateFormat2;
	}

	/**
	 * Get the JAXB unmarshaller.
	 * 
	 * @return the JAXB unmarshaller
	 */
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}
	
	public Marshaller getMarshaller() {
		return marshaller;
	}

	/**
	 * For the generation of instances from classes by JAXB a unmarshaller is
	 * needed. In the method the needed unmarshaller is created. It is said
	 * where the classes for the instantiation are.
	 * 
	 * @throws MalformedURLException
	 * @throws JAXBException
	 */
	private void createUnmarshaller() throws MalformedURLException, JAXBException {
		URL xsd = getBundle().getEntry("/schema/lgui.xsd");

		JAXBContext jc = JAXBContext.newInstance("org.eclipse.ptp.rm.lml.internal.core.elements",
				LMLCorePlugin.class.getClassLoader());

		unmarshaller = jc.createUnmarshaller();

		// if xsd is null => do not check for validity
		if (xsd != null) {

			Schema mySchema;
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			try {

				mySchema = sf.newSchema(xsd);
			} catch (SAXException saxe) {
				// ...(error handling)
				mySchema = null;

			}

			// Connect schema to unmarshaller
			unmarshaller.setSchema(mySchema);

		}
	}
	
	
	private void createMarshaller() throws JAXBException {
		URL xsd = getBundle().getEntry("/schema/lgui.xsd");

		JAXBContext jc = JAXBContext.newInstance("org.eclipse.ptp.rm.lml.internal.core.elements",
				LMLCorePlugin.class.getClassLoader());
		marshaller = jc.createMarshaller();
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Locate the fragment for our architecture. This should really be phased
	 * out, since there is now no guarantee that there will be local executables
	 * for the proxy server or debugger.
	 * 
	 * @param fragment
	 * @param file
	 * @return path to "bin" directory in fragment
	 */
	public String locateFragmentFile(String fragment, String file) {
		Bundle[] frags = Platform.getFragments(Platform.getBundle(LMLCorePlugin.PLUGIN_ID));

		if (frags != null) {
			String os = Platform.getOS();
			String arch = Platform.getOSArch();
			String frag_os_arch = fragment + "." + os + "." + arch; //$NON-NLS-1$ //$NON-NLS-2$

			for (int i = 0; i < frags.length; i++) {
				Bundle frag = frags[i];
				URL path = frag.getEntry("/"); //$NON-NLS-1$
				try {
					URL local_path = FileLocator.toFileURL(path);
					String str_path = local_path.getPath();

					/*
					 * Check each fragment that matches our os and arch for a
					 * bin directory.
					 */

					int idx = str_path.indexOf(frag_os_arch);
					if (idx > 0) {
						/*
						 * found it! This is the right fragment for our OS &
						 * arch
						 */
						String file_path = str_path + "bin/" + file; //$NON-NLS-1$
						File f = new File(file_path);
						if (f.exists()) {
							return file_path;
						}
					}

				} catch (Exception e) {
				}
			}
		}

		/* guess we never found it.... */
		return null;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		createUnmarshaller();
		simpleDateFormat1 = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
		simpleDateFormat2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
		DebugUtil.configurePluginDebugOptions();
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			public void saving(ISaveContext saveContext) throws CoreException {
				Preferences.savePreferences(getUniqueIdentifier());
			}

			public void rollback(ISaveContext saveContext) {
			}

			public void prepareToSave(ISaveContext saveContext) throws CoreException {
			}

			public void doneSaving(ISaveContext saveContext) {
			}
		});
	}

	/**
	 * This method is called when the plug-in is stopped
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

}