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
	private final String fClass;
	private final String fContributorName;
	private ISynchronizeParticipant fParticipant;

	public SynchronizeParticipantDescriptor(IConfigurationElement configElement) {
		fId = configElement.getAttribute(ATTR_ID);
		fName = configElement.getAttribute(ATTR_NAME);
		fServiceId = configElement.getAttribute(ATTR_SERVICE_ID);
		fClass = configElement.getAttribute(ATTR_CLASS);
		fContributorName = configElement.getDeclaringExtension().getContributor().getName();
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
			try {
				Class<?> cls = Platform.getBundle(fContributorName).loadClass(fClass);
				Constructor<?> cons = cls.getConstructor(ISynchronizeParticipantDescriptor.class);
				fParticipant = (ISynchronizeParticipant) cons.newInstance(this);
			} catch (Exception e) {
				RDTSyncUIPlugin.log(NLS.bind(Messages.SynchronizeParticipantDescriptor_invalidClass, new String[] { fClass, fId }),
						e);
				return null;
			}
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
