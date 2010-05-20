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
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;

public interface IPBSResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {

	/**
	 * @since 4.0
	 */
	public String getDefaultTemplateName();

	/**
	 * @since 4.0
	 */
	public void setDefaultTemplateName(String name);
}