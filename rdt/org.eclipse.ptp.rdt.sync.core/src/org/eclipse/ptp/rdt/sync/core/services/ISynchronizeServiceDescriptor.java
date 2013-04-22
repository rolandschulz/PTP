/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

/**
 * Must be implemented by extensions to the syncService extension point.
 * 
 * @since 3.0
 */
public interface ISynchronizeServiceDescriptor {
	/**
	 * Returns the unique id that identifies this service.
	 * 
	 * @return the unique id that identifies this service.
	 */
	public String getId();

	/**
	 * Returns the name of this service. This can be shown to the user.
	 * 
	 * @return the name of this service. This can be shown to the user.
	 */
	public String getName();

	/**
	 * @return
	 */
	public ISynchronizeService getService();
}
