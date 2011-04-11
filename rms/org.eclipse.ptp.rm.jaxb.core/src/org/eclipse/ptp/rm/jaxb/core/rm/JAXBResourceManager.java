/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerControl;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;

/**
 * The base JAXB resource manager, containing control and monitor parts.<br>
 * <br>
 * The JAXB implementation uses an XML configuration validated against an
 * internal schema (XSD) to configure the manager runtime and to construct the
 * related UI parts.<br>
 * <br>
 * Refer to the resource_manager_type.xsd for more details.
 * 
 * @author arossi
 * 
 */
public final class JAXBResourceManager extends AbstractResourceManager implements IJAXBResourceManager, IJAXBNonNLSConstants {
	/**
	 * @param jaxbServiceProvider
	 * @param control
	 * @param monitor
	 */
	public JAXBResourceManager(AbstractResourceManagerConfiguration jaxbServiceProvider, AbstractResourceManagerControl control,
			AbstractResourceManagerMonitor monitor) {
		super(jaxbServiceProvider, control, monitor);
	}

	/**
	 * @return the control manager associated with this base manager
	 */
	@Override
	public JAXBResourceManagerControl getControl() {
		return (JAXBResourceManagerControl) super.getControl();
	}

	/**
	 * @return the configuration (service provider)
	 */
	public IJAXBResourceManagerConfiguration getJAXBConfiguration() {
		return (IJAXBResourceManagerConfiguration) getConfiguration();
	}

}
