/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.SynchronizeServiceDescriptor;

public class SynchronizeServiceRegistry {
	public static final String SYNCHRONIZE_SERVICE_EXTENSION = "synchronizeService"; //$NON-NLS-1$
	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$

	private static Map<String, ISynchronizeServiceDescriptor> fSyncServices;

	public static ISynchronizeServiceDescriptor[] getSynchronizeServiceDescriptors() {
		loadServices();
		return fSyncServices.values().toArray(new ISynchronizeServiceDescriptor[0]);
	}

	public static ISynchronizeServiceDescriptor getSynchronizeServiceDescriptor(String id) {
		loadServices();
		return fSyncServices.get(id);
	}

	private static void loadServices() {
		if (fSyncServices == null) {
			fSyncServices = new HashMap<String, ISynchronizeServiceDescriptor>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RDTSyncCorePlugin.PLUGIN_ID,
					SYNCHRONIZE_SERVICE_EXTENSION);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement configElement : extension.getConfigurationElements()) {
						String id = configElement.getAttribute(ATTR_ID);
						if (id != null) {
							ISynchronizeServiceDescriptor desc = new SynchronizeServiceDescriptor(configElement);
							fSyncServices.put(id, desc);
						}
					}
				}
			}
		}
	}

}
