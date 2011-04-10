/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import java.net.URL;

import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;

/**
 * JAXB-specific service provider (configuration) interface.
 * 
 * @author arossi
 * 
 */
public interface IJAXBResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {
	/**
	 * Resets internal (in-memory) data objects.
	 * 
	 * @since 5.0
	 */
	void clearReferences();

	/**
	 * @return the JAXB resource manager data element tree.
	 * 
	 * @since 5.0
	 */
	ResourceManagerData getResourceManagerData();

	/**
	 * Unmarshal the XML into the JAXB resource manager data element tree.
	 * 
	 * @since 5.0
	 */
	void realizeRMDataFromXML() throws Throwable;

	/**
	 * Export the resource manager environment as the currently active one.
	 * 
	 * @since 5.0
	 */
	void setActive() throws Throwable;

	/**
	 * @param location
	 *            of the XML configuration for this resource manager.
	 * 
	 * @since 5.0
	 */
	void setRMConfigurationURL(URL location);
}
