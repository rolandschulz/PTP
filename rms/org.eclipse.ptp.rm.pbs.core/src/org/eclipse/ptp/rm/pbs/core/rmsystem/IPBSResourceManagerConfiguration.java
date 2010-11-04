/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	IBM - Initial API and implementation
 * 	Albert L. Rossi (NCSA) - modification of API (04/30/2010)
 *                         - further reworking of API (10/11/2010)
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;

public interface IPBSResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {

	/**
	 * @since 5.0
	 */
	public void addTemplate(String name, String serialized);

	/**
	 * @since 5.0
	 */
	public String getCurrentTemplateName();

	/**
	 * @since 5.0
	 */
	public String getProxyConfiguration();

	/**
	 * @since 5.0
	 */
	public String getTemplate(String name);

	/**
	 * @since 5.0
	 */
	public String[] getTemplateNames();

	/**
	 * @since 5.0
	 */
	public String getValidAttributeSet();

	/**
	 * @since 5.0
	 */
	public void removeTemplate(String name);

	/**
	 * @since 5.0
	 */
	public void removeValidAttributeSet();

	/**
	 * @since 5.0
	 */
	public void setCurrentTemplateName(String name);

	/**
	 * @since 5.0
	 */
	public void setProxyConfiguration(String type);

	/**
	 * @since 5.0
	 */
	public void setValidAttributeSet(String serialized);

}