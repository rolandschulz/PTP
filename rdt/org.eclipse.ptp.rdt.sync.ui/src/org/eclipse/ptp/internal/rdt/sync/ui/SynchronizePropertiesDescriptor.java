/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor;

public class SynchronizePropertiesDescriptor implements ISynchronizePropertiesDescriptor {
	public static final String ATTR_NATURE = "nature"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private final String fNature;
	private final String fClass;
	private final String fContributorName;
	private ISynchronizeProperties fProperties;

	public SynchronizePropertiesDescriptor(IConfigurationElement configElement) {
		fNature = configElement.getAttribute(ATTR_NATURE);
		fClass = configElement.getAttribute(ATTR_CLASS);
		fContributorName = configElement.getDeclaringExtension().getContributor().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor#getNature()
	 */
	@Override
	public String getNature() {
		return fNature;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizePropertiesDescriptor#getProperties()
	 */
	@Override
	public ISynchronizeProperties getProperties() {
		if (fProperties == null) {
			try {
				Class<?> cls = Platform.getBundle(fContributorName).loadClass(fClass);
				Constructor<?> cons = cls.getConstructor(ISynchronizePropertiesDescriptor.class);
				fProperties = (ISynchronizeProperties) cons.newInstance(this);
			} catch (Exception e) {
				RDTSyncUIPlugin.log(NLS.bind("Class {0} does not implement ISynchronizeProperties", fClass), e); //$NON-NLS-1$
				return null;
			}
		}
		return fProperties;
	}
}
