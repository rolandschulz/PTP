/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 *******************************************************************************/
package org.eclipse.ptp.rm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.internal.rm.core.ResourceManagerLog;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerChangedListener;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ResourceManagerPlugin extends Plugin {

	public static final String ID = "org.eclipse.ptp.rm.core";

	private static final String ATT_CLASS = "class";

	private static ResourceManagerPlugin plugin;

	public static ResourceManagerPlugin getDefault() {
		return plugin;
	}

	private ResourceManagerFactory currentFactory = null;

	private IRMResourceManager currentManager = null;

	private ResourceManagerFactory[] factories = new ResourceManagerFactory[0];

	private final List listeners = new ArrayList();

	public ResourceManagerPlugin() {
		super();
		plugin = this;
	}

	public synchronized void addResourceManagerChangedListener(
			IRMResourceManagerChangedListener listener) {
		listeners.add(listener);
	}

	public ResourceManagerFactory getCurrentFactory() throws CoreException {

		getFactories();
		
		if (factories.length == 0) {
			return null;
		}

		if (currentFactory == null) {
			setCurrentFactory(factories[0]);
		}

		return currentFactory;
	}

	public IRMResourceManager getCurrentManager() throws CoreException {
		// TODO need to retrieve manager from preferences or
		// the Resource Managers View of org.eclipse.ptp.rm.ui
		if (currentManager == null) {
			currentManager = getCurrentFactory().newResourceManager("xxx");
			fireResourceManagerChanged(null, currentManager);
		}
		return currentManager;
	}

	/**
	 * Generates the resource manager factories that been extended into this
	 * plug-n's resource manager extension point.
	 * 
	 * @return the resource manager factories for this plug-in
	 * @throws CoreException
	 */
	public ResourceManagerFactory[] getFactories() throws CoreException {

		if (factories.length > 0) {
			return factories;
		}

		IExtension[] extensions = Platform.getExtensionRegistry()
				.getExtensionPoint(ID, "resourceManagers").getExtensions();
		if (extensions.length == 0) {
			return noFactoriesError();
		}

		final List factoryList = new ArrayList();

		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			if (configElements.length == 0) {
				final String message = "No factories found in extension "
						+ extensions[i].getExtensionPointUniqueIdentifier();
				ResourceManagerLog.logInfo(message);
			}
			for (int j = 0; j < configElements.length; ++j) {
				factoryList.add(getFactory(configElements[j]));
			}
		}

		factories = (ResourceManagerFactory[]) factoryList
				.toArray(new ResourceManagerFactory[factoryList.size()]);
		if (factories.length == 0) {
			return noFactoriesError();
		}
		return factories;
	}

	public synchronized void removeResourceManagerChangedListener(
			IRMResourceManagerChangedListener listener) {
		listeners.remove(listener);
	}

	public void setCurrentFactory(ResourceManagerFactory factory)
			throws CoreException {

		// TODO this needs a lot of work

		if (!Arrays.asList(factories).contains(factory)) {
			final IllegalArgumentException exc = new IllegalArgumentException(
					"factory is not found in list");
			final IStatus status = ResourceManagerLog.createErrorStatus(exc
					.getMessage(), exc);
			throw new CoreException(status);
		}
		if (factory != currentFactory) {
			if (currentManager != null) {
				final IRMResourceManager oldManager = currentManager;
				currentManager = factory.newResourceManager("xxx");
				fireResourceManagerChanged(oldManager, currentManager);
				oldManager.dispose();
			}
		}
		currentFactory = factory;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		disposeOfFactories();
		plugin = null;
	}

	private void disposeOfFactories() {
		for (int i = 0; i < factories.length; ++i) {
			ResourceManagerFactory factory = factories[i];
			factory.dispose();
		}
	}

	private synchronized void fireResourceManagerChanged(
			IRMResourceManager oldManager, IRMResourceManager newManager) {
		final List listeners = new ArrayList(this.listeners);
		for (Iterator lit = listeners.iterator(); lit.hasNext();) {
			IRMResourceManagerChangedListener listener = (IRMResourceManagerChangedListener) lit
					.next();
			listener.resourceManagerChanged(oldManager, newManager);
		}
	}

	private ResourceManagerFactory getFactory(IConfigurationElement element) {
		try {
			return ((ResourceManagerFactory) element
					.createExecutableExtension(ATT_CLASS));
		} catch (Exception e) {
			ResourceManagerLog.logError(
					"While creating a factory from extension", e);
			return null;
		}
	}

	private ResourceManagerFactory[] noFactoriesError() throws CoreException {
		final String message = "No resourceManagers factory extensions";
		final IllegalStateException illegalStateException = new IllegalStateException(
				message);
		IStatus status = ResourceManagerLog.createErrorStatus(message,
				illegalStateException);
		throw new CoreException(status);
	}
}
