/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.internal.gem;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ptp.internal.gem.util.GemUtilities;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The activator class for this plug-in. This controls the plug-in life cycle.
 */
public class GemPlugin extends AbstractUIPlugin {

	// The shared instance
	private static GemPlugin plugin;

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.gem"; //$NON-NLS-1$

	// The configuration preferences
	private IEclipsePreferences gemPreferences;

	// The image cache for this plug-in
	private static Map<ImageDescriptor, Image> imageCache = new HashMap<ImageDescriptor, Image>();

	/**
	 * Returns the shared instance.
	 * 
	 * @param none
	 * @return GemPlugin The shared plug-in instance.
	 */
	public static GemPlugin getDefault() {
		return plugin;
	}

	/**
	 * Gets the requested Image resource.
	 * 
	 * @param imageDescriptor
	 *            The ImagesDescriptor mapped to its Image object.
	 * @return Image The requested Image.
	 */
	public static Image getImage(ImageDescriptor imageDescriptor) {
		if (imageDescriptor == null) {
			return null;
		}
		Image image = imageCache.get(imageDescriptor);
		if (image == null) {
			image = imageDescriptor.createImage();
			imageCache.put(imageDescriptor, image);
		}
		return image;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            The path to the shared resource.
	 * @return ImageDescriptor The image descriptor.
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Constructor.
	 */
	public GemPlugin() {
		super();
	}

	/**
	 * Answer the configuration location for this plug-in.
	 * 
	 * @param none
	 * @return URI The GEM plugin's configuration directory (not <code>null</code>)
	 */
	public URI getConfigDir() {
		final Location location = Platform.getConfigurationLocation();
		if (location != null) {
			final URL configURL = location.getURL();
			if (configURL != null && configURL.getProtocol().startsWith("file")) { //$NON-NLS-1$
				try {
					return configURL.toURI();
				} catch (final URISyntaxException e) {
					GemUtilities.logExceptionDetail(e);
				}
			}
		}
		return null;
	}

	/**
	 * Answer the configuration preferences shared among multiple workspaces.
	 * 
	 * @param none
	 * @return Preferences The configuration preferences or <code>null</code> if
	 *         the configuration directory is read-only or unspecified.
	 */
	public Preferences getConfigPrefs() {
		if (this.gemPreferences == null) {
			this.gemPreferences = (IEclipsePreferences) ConfigurationScope.INSTANCE;
		}
		return this.gemPreferences;
	}

	/**
	 * Return the plug-in's version.
	 * 
	 * @param none
	 * @return Version The current version of this plug-in.
	 */
	public Version getVersion() {
		return new Version(getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION));
	}

	/**
	 * Save the configuration preferences if they have been loaded.
	 * 
	 * @param none
	 * @return void
	 */
	public void saveConfigPrefs() {
		if (this.gemPreferences != null) {
			try {
				this.gemPreferences.flush();
			} catch (final BackingStoreException e) {
				GemUtilities.logExceptionDetail(e);
			}
		}
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		saveConfigPrefs();
		super.start(context);
		plugin = this;
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

		// Clear the image cache
		Iterator<Image> iter = imageCache.values().iterator();
		while (iter.hasNext()) {
			iter.next().dispose();
		}

		imageCache.clear();
		iter = null;
		imageCache = null;
	}

}
