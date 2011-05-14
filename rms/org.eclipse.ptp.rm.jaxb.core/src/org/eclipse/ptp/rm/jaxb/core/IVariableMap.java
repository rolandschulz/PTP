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

/**
 * High-level interface for resolver environments.
 * 
 * Implementations:
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap
 * @see org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap
 * 
 * @author arossi
 * 
 */
public interface IVariableMap {

	/**
	 * Empties map contents.
	 */
	public void clear();

	/**
	 * @param name
	 *            of variable
	 * @return value of variable
	 */
	public Object get(String name);

	/**
	 * @return the map containing any variables added through discovery
	 *         processes, or <code>null</code> if unsupported
	 */
	public Map<String, Object> getDiscovered();

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

	/**
	 * @return the map containing the (main) variables
	 */
	public Map<String, Object> getVariables();

	/**
	 * @param name
	 *            of variable
	 * @param value
	 *            of variable
	 */
	public void put(String name, Object value);

	/**
	 * @param name
	 *            of variable
	 * @return value of removed variable
	 */
	public Object remove(String name);

	/**
	 * @param initialized
	 *            indicates the map has already been initialized
	 */
	public void setInitialized(boolean initialized);
}
