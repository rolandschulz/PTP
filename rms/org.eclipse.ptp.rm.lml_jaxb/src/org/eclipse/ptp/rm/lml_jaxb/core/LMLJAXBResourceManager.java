/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml_jaxb.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerControl;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;

/**
 * The base LMLJAXB resource manager, containing control and monitor parts.<br>
 * <br>
 * The JAXB implementation uses an XML configuration validated against an internal schema (XSD) to configure the manager runtime and
 * to construct the related UI parts.<br>
 * <br>
 * Refer to the resource_manager_type.xsd for more details.
 * 
 * @see org.eclipse.ptp.rm.core/data/resource_manager_type.xsd)
 * 
 * @author arossi
 * 
 */
public class LMLJAXBResourceManager extends AbstractResourceManager implements IJAXBResourceManager {
	/**
	 * @param configuration
	 * @param control
	 * @param monitor
	 */
	public LMLJAXBResourceManager(AbstractResourceManagerConfiguration configuration, AbstractResourceManagerControl control,
			AbstractResourceManagerMonitor monitor) {
		super(configuration, control, monitor);
	}

	/**
	 * @return the control sub-manager.
	 */
	@Override
	public IJAXBResourceManagerControl getControl() {
		return (IJAXBResourceManagerControl) super.getControl();
	}

	/**
	 * @return the configuration
	 */
	public IJAXBResourceManagerConfiguration getJAXBConfiguration() {
		return (IJAXBResourceManagerConfiguration) getConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse .core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		IJAXBResourceManagerConfiguration conf = (IJAXBResourceManagerConfiguration) getMonitorConfiguration();
		if (conf.getUseDefault()) {
			conf.setRemoteServicesId(getControlConfiguration().getRemoteServicesId());
			conf.setConnectionName(getControlConfiguration().getConnectionName());
		}
		super.doStartup(monitor);
		fireResourceManagerStarted();
	}
}
