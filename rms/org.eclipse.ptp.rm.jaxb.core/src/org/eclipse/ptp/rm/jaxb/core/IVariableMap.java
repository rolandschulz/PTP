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

/**
 * High-level interface for resolver environments.
 * 
 * Implementations:
 * 
 * @see org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap
 * @see org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap
 * 
 * @author arossi
 * 
 */
public interface IVariableMap {

	/**
	 * @param value
	 *            expression to resolve.
	 * @return resolved expression
	 */
	public String getString(String value);

	/**
	 * @param uuid
	 *            internal identifier associate with a job submission
	 * @param value
	 *            expression to resolve.
	 * @return resolved expression
	 */
	public String getString(String uuid, String value);
}
