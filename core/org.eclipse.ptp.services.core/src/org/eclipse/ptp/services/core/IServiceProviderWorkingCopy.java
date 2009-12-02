/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import org.eclipse.core.runtime.IAdaptable;


/**
 * An IServiceProviderWorkingCopy represents an editable copy of
 * and IServiceProvider. Properties of a service provider can
 * be modified by modifying the properties of a working
 * copy and then saving the working copy.
 */
public interface IServiceProviderWorkingCopy extends IServiceProvider, IAdaptable {
	/**
	 * Check if any properties have been modified.
	 * 
	 * @return true if the properties need to be saved
	 */
	public boolean isDirty();
	
	/**
	 * Saves the modified properties.
	 */
	public void save();
}
