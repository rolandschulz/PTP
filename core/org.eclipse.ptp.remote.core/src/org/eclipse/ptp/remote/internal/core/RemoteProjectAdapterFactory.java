/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.remote.core.IRemoteProject;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

public class RemoteProjectAdapterFactory implements IAdapterFactory {
	public static final String EXTENSION_POINT_ID = "remoteProjects"; //$NON-NLS-1$

	public static final String ATTR_NATURE = "nature"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private Map<String, RemoteProjectFactory> fProjectFactory;

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IRemoteProject.class) {
			if (adaptableObject instanceof IProject) {
				loadExtensions();
				IProject project = (IProject) adaptableObject;
				for (String nature : fProjectFactory.keySet()) {
					try {
						if (project.hasNature(nature)) {
							RemoteProjectFactory factory = fProjectFactory.get(nature);
							if (factory != null) {
								return factory.getRemoteProject(project);
							}
						}
					} catch (CoreException e) {
						// Treat as failure
					}
				}
				return new LocalProject(project);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IRemoteProject.class };
	}

	private synchronized void loadExtensions() {
		if (fProjectFactory == null) {
			fProjectFactory = new HashMap<String, RemoteProjectFactory>();

			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(PTPRemoteCorePlugin.getUniqueIdentifier(),
					EXTENSION_POINT_ID);

			for (IExtension ext : extensionPoint.getExtensions()) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					String nature = ce.getAttribute(ATTR_NATURE);
					RemoteProjectFactory factory = new RemoteProjectFactory(ce);
					fProjectFactory.put(nature, factory);
				}
			}
		}
	}

}
