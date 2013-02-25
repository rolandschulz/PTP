/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.remote.rse.core;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.internal.connectorservice.local.LocalConnectorServiceManager;
import org.eclipse.rse.internal.services.local.ILocalService;

/**
 * @author crecoskie
 *
 */
@SuppressWarnings("restriction")
public class LocalSpawnerSubsystemConfiguration extends SubSystemConfiguration {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(org.eclipse.rse.core.model.IHost)
	 */
	@Override
	public ISubSystem createSubSystemInternal(IHost conn) {
		return new LocalSpawnerSubsystem(conn, getConnectorService(conn));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getServiceImplType()
	 */
	@Override
	public Class getServiceImplType() {
		return ILocalService.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.rse.core.subsystems.SubSystemConfiguration#getConnectorService
	 * (org.eclipse.rse.core.model.IHost)
	 */
	@Override
	public IConnectorService getConnectorService(IHost host) {
		return LocalConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.rse.core.subsystems.SubSystemConfiguration#setConnectorService
	 * (org.eclipse.rse.core.model.IHost,
	 * org.eclipse.rse.core.subsystems.IConnectorService)
	 */
	@Override
	public void setConnectorService(IHost host, IConnectorService connectorService) {
		LocalConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
		ISubSystem[] sses = getSubSystems(host, false);
		if (sses != null && sses.length > 0) {
			for (int i = 0; i < sses.length; i++) {
				ISubSystem ss = sses[i];
				ss.setConnectorService(connectorService);
			}
		}
	}
	
}
