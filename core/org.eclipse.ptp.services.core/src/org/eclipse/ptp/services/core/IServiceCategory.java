/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import java.util.Set;

/**
 * Services can be organized into categories. 
 * This is mainly so that services can be presented to the user
 * in an organized way.
 */
public interface IServiceCategory {
	
	
	/**
	 * Get the ID of the service category.
	 */
	public String getId();
	
	/**
	 * Get the name of this service category.
	 */
	public String getName();
	
	/**
	 * Get a set of all the services in the category.
	 * @return
	 */
	public Set<IService> getServices();
}
