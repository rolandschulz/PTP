/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.variables;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMConstants;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;

/**
 * Abstraction representing all the Property and Attribute definitions
 * associated with a resource manager configuration. This provides an
 * "environment" for dereferencing the resource manager configuration, such that
 * the values of its Properties and Attributes can reference other Properties
 * and Attributes.<br>
 * <br>
 * 
 * There are two submaps: one contains all the Properties and Attributes defined
 * in the XML configuration; a separate map contains all the Properties and
 * Attributes which are "discovered" at runtime (through tokenization of command
 * output).
 * 
 * @author arossi
 * 
 */
public class RMVariableMap implements IVariableMap {
	private static final Object monitor = new Object();

	private final Map<String, Object> variables;
	private final Map<String, Object> discovered;
	private boolean initialized;

	public RMVariableMap() {
		variables = Collections.synchronizedMap(new TreeMap<String, Object>());
		discovered = Collections.synchronizedMap(new TreeMap<String, Object>());
		initialized = false;
	}

	/**
	 * Reset the internal maps.
	 */
	public void clear() {
		variables.clear();
		discovered.clear();
		initialized = false;
	}

	/**
	 * Search the two maps for this reference. The predefined variables are
	 * searched first.
	 * 
	 * @param name
	 *            of Property or Attribute to find.
	 * @return the found Property or Attribute
	 */
	public Object get(String name) {
		Object o = variables.get(name);
		if (o == null) {
			o = discovered.get(name);
		}
		return o;
	}

	/**
	 * @return map of Properties and Attributes discovered at runtime.
	 */
	public Map<String, Object> getDiscovered() {
		return discovered;
	}

	/**
	 * Delegates to {@link #getString(String)}.
	 * 
	 * @param value
	 *            expression to resolve
	 * @return resolved expression
	 */
	public String getString(String value) {
		return getString(null, value);
	}

	/**
	 * Performs the substitution on the string.
	 * 
	 * @param value
	 *            expression to resolve
	 * @return resolved expression
	 */
	public String getString(String jobId, String value) {
		try {
			if (jobId != null) {
				value = value.replaceAll(JAXBRMConstants.JOB_ID_TAG, jobId);
			}
			return dereference(value);
		} catch (CoreException t) {
			JAXBCorePlugin.log(t);
		}
		return value;
	}

	/**
	 * @return map of Properties and Attributes defined in the XML
	 *         configuration.
	 */
	public Map<String, Object> getVariables() {
		return variables;
	}

	/**
	 * @return whether the map has been initialized from an XML resource manager
	 *         configuration.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Only creates property and adds it if value is not <code>null</code>.
	 * 
	 * @param name
	 *            of property to add
	 * @param value
	 *            of property to add
	 * @param visible
	 *            whether this property is to be made available through the user
	 *            interface (Launch Tab)
	 */
	public void maybeAddProperty(String name, Object value, boolean visible) {
		if (value == null) {
			return;
		}
		PropertyType p = new PropertyType();
		p.setName(name);
		p.setValue(value);
		p.setVisible(visible);
		variables.put(name, p);
	}

	/**
	 * Looks for property in the configuration and uses it to create a Property
	 * in the current environment. If the property is undefined or has a
	 * <code>null</code> value in the configuration, this will not overwrite a
	 * currently defined property in the environment. <br>
	 * <br>
	 * Delegates to {@link #maybeAddProperty(String, Object, boolean)}
	 * 
	 * @param key1
	 *            to map the property to in the environemnt
	 * @param key2
	 *            to search for the property in the configuration
	 * @param configuration
	 *            to search
	 * @throws CoreException
	 */
	public void maybeOverwrite(String key1, String key2, ILaunchConfiguration configuration) throws CoreException {
		Object value1 = null;
		Object value2 = null;
		PropertyType p = (PropertyType) variables.get(key1);
		if (p != null) {
			value2 = p.getValue();
		}
		value2 = configuration.getAttributes().get(key2);
		if (value2 == null) {
			maybeAddProperty(key1, value1, false);
		} else {
			maybeAddProperty(key1, value2, false);
		}
	}

	/**
	 * Places a Property or Attribute directly in the environment.
	 * 
	 * @param name
	 *            of Property or Attribute
	 * @param value
	 *            of Property or Attribute
	 */
	public void put(String name, Object value) {
		variables.put(name, value);
	}

	/**
	 * Removes a Property or Attribute. Checks first in the predefined values
	 * map, and if it does not exist there, removes from the runtime values map.
	 * 
	 * @param name
	 *            of Property or Attribute to remove
	 * @return value of Property or Attribute
	 */
	public Object remove(String name) {
		Object o = variables.remove(name);
		if (o == null) {
			o = discovered.remove(name);
		}
		return o;
	}

	/**
	 * @param initialized
	 *            whether the map has been initialized from an XML resource
	 *            manager configuration.
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Calls the string substitution method on the variable manager. Under
	 * synchronization, sets the variable resolver's map reference to this
	 * instance.
	 * 
	 * @param expression
	 *            to be resolved (recursively dereferenced from the map).
	 * @return the resolved expression
	 * @throws CoreException
	 */
	private String dereference(String expression) throws CoreException {
		if (expression == null) {
			return null;
		}
		synchronized (monitor) {
			RMVariableResolver.setActive(this);
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
		}
	}
}
