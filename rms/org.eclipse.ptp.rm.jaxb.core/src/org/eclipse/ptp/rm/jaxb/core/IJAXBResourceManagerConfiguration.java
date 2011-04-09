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

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;

public interface IJAXBResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {
	/**
	 * @since 5.0
	 */
	void clearReferences();

	/**
	 * @since 5.0
	 */
	String getDefaultControlHost();

	/**
	 * @since 5.0
	 */
	String getDefaultControlPath();

	/**
	 * @since 5.0
	 */
	String getDefaultControlPort();

	/**
	 * @since 5.0
	 */
	String getDefaultMonitorHost();

	/**
	 * @since 5.0
	 */
	String getDefaultMonitorPath();

	/**
	 * @since 5.0
	 */
	String getDefaultMonitorPort();

	/**
	 * @since 5.0
	 */

	/**
	 * @since 5.0
	 */
	ResourceManagerData getResourceManagerData();

	/**
	 * @since 5.0
	 */
	IRemoteServices getService();

	/**
	 * @since 5.0
	 */
	void realizeRMDataFromXML() throws Throwable;

	/**
	 * @since 5.0
	 */
	void setActive() throws Throwable;

	/**
	 * @since 5.0
	 */
	void setRMConfigurationURL(URL location);

	/**
	 * @since 5.0
	 */
	void setService(IRemoteServices service);
}
