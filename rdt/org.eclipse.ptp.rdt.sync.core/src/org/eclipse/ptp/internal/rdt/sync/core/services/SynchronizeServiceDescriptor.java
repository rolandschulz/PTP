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
package org.eclipse.ptp.internal.rdt.sync.core.services;

import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor;

public class SynchronizeServiceDescriptor implements ISynchronizeServiceDescriptor {
	private final String fName;
	private final String fId;
	private ISynchronizeService fService;

	public SynchronizeServiceDescriptor(IConfigurationElement configElement) {
		fId = configElement.getAttribute(SynchronizeServiceRegistry.ATTR_ID);
		fName = configElement.getAttribute(SynchronizeServiceRegistry.ATTR_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getId()
	 */
	@Override
	public String getId() {
		return fId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor#getService()
	 */
	@Override
	public ISynchronizeService getService() {
		if (fService == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RDTSyncCorePlugin.PLUGIN_ID,
					SynchronizeServiceRegistry.SYNCHRONIZE_SERVICE_EXTENSION);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement configElement : extension.getConfigurationElements()) {
						String extensionId = configElement.getAttribute(SynchronizeServiceRegistry.ATTR_ID);
						if (extensionId != null && extensionId.equals(fId)) {
							try {
								String serviceClass = configElement.getAttribute(SynchronizeServiceRegistry.ATTR_CLASS);
								Class<?> cls = Platform.getBundle(configElement.getDeclaringExtension().getContributor().getName())
										.loadClass(serviceClass);
								Constructor<?> cons = cls.getConstructor(ISynchronizeServiceDescriptor.class);
								return (ISynchronizeService) cons.newInstance(this);
							} catch (Exception e) {
								String className = configElement.getAttribute(SynchronizeServiceRegistry.ATTR_CLASS);
								RDTSyncCorePlugin.log(NLS.bind(Messages.SynchronizeServiceDescriptor_Invalid_class, new String[] {
										className, fId }), e);
							}
						}
					}
				}
			}
			return null;
		}
		return fService;
	}
}
