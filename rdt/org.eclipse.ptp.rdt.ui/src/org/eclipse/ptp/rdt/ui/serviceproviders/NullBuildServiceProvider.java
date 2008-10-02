/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceProviderDescriptor;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * A build service provider that does nothing.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 *
 */
public class NullBuildServiceProvider extends ServiceProviderDescriptor
		implements IServiceProvider {
	
	public static final String ID = "org.eclipse.ptp.rdt.ui.NullBuildServiceProvider"; //$NON-NLS-1$
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("NullBuildServiceProvider.name"); //$NON-NLS-1$

	
	public NullBuildServiceProvider(String id, String name, String serviceId) {
		super(id, name, serviceId);
	}
	
	public NullBuildServiceProvider() {
		this(ID, NAME, SERVICE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#getConfigurationString()
	 */
	public String getConfigurationString() {
		return Messages.getString("NullServiceProvider.config"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		// does not restore anything
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.services.core.IServiceProvider#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		// does not save anything
	}

}
