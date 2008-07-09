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
package org.eclipse.ptp.rdt.ui.subsystems;

import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.services.dstore.IDStoreService;

/**
 * @author crecoskie
 *
 */
public class RemoteCIndexSubsystemConfiguration extends SubSystemConfiguration
		implements ISubSystemConfiguration {

	/**
	 * 
	 */
	public RemoteCIndexSubsystemConfiguration() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(org.eclipse.rse.core.model.IHost)
	 */
	@Override
	public ISubSystem createSubSystemInternal(IHost conn) {
		return new RemoteCIndexSubsystem(conn, getConnectorService(conn));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getConnectorService(org.eclipse.rse.core.model.IHost)
	 */
	@Override
	public IConnectorService getConnectorService(IHost host)
	{
		return DStoreConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getServiceImplType()
	 */
	public Class<IDStoreService> getServiceImplType()
	{
		return IDStoreService.class;
	}
}
