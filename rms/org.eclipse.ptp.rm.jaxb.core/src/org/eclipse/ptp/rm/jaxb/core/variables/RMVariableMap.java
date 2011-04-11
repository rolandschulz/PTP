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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Property;

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
public class RMVariableMap implements IVariableMap, IJAXBNonNLSConstants {
	private static RMVariableMap active;

	private final Map<String, Object> variables;
	private final Map<String, Object> discovered;
	private boolean initialized;

	private RMVariableMap() {
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
				value = value.replaceAll(JOB_ID_TAG, jobId);
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
		Property p = new Property();
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
	@SuppressWarnings("rawtypes")
	public void maybeOverwrite(String key1, String key2, ILaunchConfiguration configuration) throws CoreException {
		Object value = null;
		Property p = (Property) variables.get(key1);
		if (p != null) {
			value = p.getValue();
		}

		if (value instanceof Integer) {
			value = configuration.getAttribute(key2, (Integer) value);
		} else if (value instanceof Boolean) {
			value = configuration.getAttribute(key2, (Boolean) value);
		} else if (value instanceof String) {
			value = configuration.getAttribute(key2, (String) value);
		} else if (value instanceof List) {
			value = configuration.getAttribute(key2, (List) value);
		} else if (value instanceof Map) {
			value = configuration.getAttribute(key2, (Map) value);
		}

		maybeAddProperty(key1, value, false);
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
	 * Calls the string substitution method on the variable manager.
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
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	/**
	 * @return the currently active instance.
	 */
	public synchronized static RMVariableMap getActiveInstance() {
		return active;
	}

	/**
	 * @param instance
	 *            if <code>null</code>, an empty map is created, set to the
	 *            active map, and returned. Else the passed-in instance is set
	 *            and returned.
	 * @return the resulting active instance
	 */
	public synchronized static RMVariableMap setActiveInstance(RMVariableMap instance) {
		if (instance == null) {
			instance = new RMVariableMap();
		}
		RMVariableMap.active = instance;
		return RMVariableMap.active;
	}
}
