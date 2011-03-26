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

import java.util.Map;

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

	String[] getExternalRMInstanceXMLLocations();

	/**
	 * @since 5.0
	 */
	String getRMInstanceXMLLocation();

	/**
	 * @since 5.0
	 */
	Map<String, String> getSelectedAttributeSet();

	/**
	 * @since 5.0
	 */
	IRemoteServices getService();

	/**
	 * @since 5.0
	 */
	String getValidAttributeSet();

	/**
	 * @since 5.0
	 */
	void realizeRMDataFromXML() throws Throwable;

	/**
	 * @since 5.0
	 */
	void removeSelectedAttributeSet();

	/**
	 * @since 5.0
	 */
	void removeValidAttributeSet();

	/**
	 * @since 5.0
	 */
	ResourceManagerData getResourceManagerData();

	/**
	 * @since 5.0
	 */
	void setActive() throws Throwable;

	/**
	 * @since 5.0
	 */
	void setExternalRMInstanceXMLLocations(String[] location);

	/**
	 * @since 5.0
	 */
	void setRMInstanceXMLLocation(String location);

	/**
	 * @since 5.0
	 */
	void setSelectedAttributeSet(Map<String, String> map);

	/**
	 * @since 5.0
	 */
	void setService(IRemoteServices service);

	/**
	 * @since 5.0
	 */
	void setValidAttributeSet(String serialized);
}
