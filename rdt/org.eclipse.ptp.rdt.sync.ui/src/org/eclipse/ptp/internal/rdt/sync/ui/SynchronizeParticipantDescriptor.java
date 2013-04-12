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
package org.eclipse.ptp.internal.rdt.sync.ui;

import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;

public class SynchronizeParticipantDescriptor implements ISynchronizeParticipantDescriptor {
	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_SERVICE_ID = "serviceId"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private final String fName;
	private final String fId;
	private final String fServiceId;
	private ISynchronizeParticipant fParticipant;

	public SynchronizeParticipantDescriptor(IConfigurationElement configElement) {
		fId = configElement.getAttribute(ATTR_ID);
		fName = configElement.getAttribute(ATTR_NAME);
		fServiceId = configElement.getAttribute(ATTR_SERVICE_ID);
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
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getParticipant
	 * ()
	 */
	@Override
	public ISynchronizeParticipant getParticipant() {
		if (fParticipant == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RDTSyncUIPlugin.PLUGIN_ID,
					RDTSyncUIPlugin.SYNCHRONIZE_EXTENSION);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement configElement : extension.getConfigurationElements()) {
						String extensionId = configElement.getAttribute(ATTR_ID);
						if (extensionId != null && extensionId.equals(fId)) {
							try {
								String serviceClass = configElement.getAttribute(ATTR_CLASS);
								Class<?> cls = Platform.getBundle(configElement.getDeclaringExtension().getContributor().getName())
										.loadClass(serviceClass);
								Constructor<?> cons = cls.getConstructor(ISynchronizeParticipantDescriptor.class);
								return (ISynchronizeParticipant) cons.newInstance(this);
							} catch (Exception e) {
								String className = configElement.getAttribute(ATTR_CLASS);
								RDTSyncUIPlugin.log(
										NLS.bind(Messages.SynchronizeParticipantDescriptor_invalidClass, new String[] { className,
												fId }), e);
							}
							return null;
						}
					}
				}
			}
			return null;
		}
		return fParticipant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getServiceId()
	 */
	@Override
	public String getServiceId() {
		return fServiceId;
	}
}
