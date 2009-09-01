/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration;

public interface IPBSResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {

	/**
	 * @return the pbsdArgs
	 */
	public String getPBSdArgs();

	/**
	 * @return the pbsdPath
	 */
	public String getPBSdPath();
	
	/**
	 * @return the useDefaults
	 */
	public boolean getUseDefaults();

	/**
	 * @param pbsdArguments
	 *            the pbsdArgs to set
	 */
	public void setPBSdArgs(String pbsdArgs);
	
	/**
	 * @param pbsdPath
	 *            the pbsdPath to set
	 */
	public void setPBSdPath(String pbsdPath);

	/**
	 * @param useDefaults
	 *            the useDefaults to set
	 */
	public void setUseDefaults(boolean useDefaults);
}