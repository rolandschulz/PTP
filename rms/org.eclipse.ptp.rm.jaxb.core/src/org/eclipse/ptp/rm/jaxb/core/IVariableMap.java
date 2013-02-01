/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;

/**
 * High-level interface for resolver environments.
 * 
 * Implementations:
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap
 * @see org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public interface IVariableMap {

	/**
	 * Empties map contents.
	 */
	public void clear();

	/**
	 * Search for an attribute.
	 * 
	 * @param name
	 *            of attribute to find.
	 * @return the found attribute
	 */
	public AttributeType get(String name);

	/**
	 * Get the default value of the attribute
	 * 
	 * @param name
	 * @return
	 */
	public String getDefault(String name);

	/**
	 * Get the map of attributes discovered at runtime.
	 * 
	 * @return the map containing any variables added through discovery processes, or <code>null</code> if unsupported
	 */
	public Map<String, AttributeType> getDiscovered();

	/**
	 * Get the environment manager associated with this map.
	 * 
	 * @return environment manager
	 */
	public IEnvManager getEnvManager();

	/**
	 * Get the string representation of an expression.
	 * 
	 * @param value
	 *            expression to resolve.
	 * @return resolved expression
	 */
	public String getString(String value);

	/**
	 * Get the string representation of an expression. Substitutes any occurrences of {@link JAXBControlConstants#JOB_ID_TAG} with
	 * the uuid prior to evaluating the expression.
	 * 
	 * @param uuid
	 *            internal identifier associate with a job submission (can be <code>null</code>)
	 * @param value
	 *            expression to resolve.
	 * @return resolved expression
	 */
	public String getString(String uuid, String value);

	/**
	 * Get the map containing the variables.
	 * 
	 * @return map containing the variables
	 */
	public Map<String, AttributeType> getAttributes();

	/**
	 * Places a attribute directly in the environment.
	 * 
	 * @param name
	 *            of variable
	 * @param value
	 *            of variable
	 */
	public void put(String name, AttributeType value);

	/**
	 * Removes an attribute. Checks first in the predefined values map, and if it does not exist there, removes from the runtime
	 * values map.
	 * 
	 * @param name
	 *            of attribute
	 * @return value of removed variable
	 */
	public AttributeType remove(String name);

	/**
	 * Sets the default value of the attribute.
	 * 
	 * @param name
	 *            of variable
	 * @param defaultValue
	 *            of variable
	 */
	public void setDefault(String name, String defaultValue);

	/**
	 * Set a flag to indicate the map has been initialized
	 * 
	 * @param initialized
	 *            indicates the map has already been initialized
	 */
	public void setInitialized(boolean initialized);
}
