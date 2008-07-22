/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.core;



public interface IRemoteServices extends IRemoteServicesDelegate {
	/**
	 * Get unique ID of this service. Can be used as a lookup key.
	 * 
	 * @return unique ID
	 */
	public String getId();
	
	/**
	 * Get display name of this service.
	 * 
	 * @return display name
	 */
	public String getName();
	
	/**
	 * Get state of this service
	 * 
	 * @return true if initialized successfully
	 */
	public boolean isInitialized();
}
