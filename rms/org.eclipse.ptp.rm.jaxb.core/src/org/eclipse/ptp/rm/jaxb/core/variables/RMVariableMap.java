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

	public void clear() {
		variables.clear();
		discovered.clear();
		initialized = false;
	}

	public Object get(String name) {
		Object o = variables.get(name);
		if (o == null) {
			o = discovered.get(name);
		}
		return o;
	}

	public Map<String, Object> getDiscovered() {
		return discovered;
	}

	public String getString(String value) {
		return getString(null, value);
	}

	/**
	 * Performs the substitution on the string. rm: prefix points to the
	 * RMVariableResolver.
	 * 
	 * @param expression
	 * @return
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

	public Map<String, Object> getVariables() {
		return variables;
	}

	public boolean isInitialized() {
		return initialized;
	}

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

	public void put(String name, Object value) {
		variables.put(name, value);
	}

	public Object remove(String name) {
		Object o = variables.remove(name);
		if (o == null) {
			o = discovered.remove(name);
		}
		return o;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	private String dereference(String expression) throws CoreException {
		if (expression == null) {
			return null;
		}
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	public synchronized static RMVariableMap getActiveInstance() {
		return active;
	}

	public synchronized static RMVariableMap setActiveInstance(RMVariableMap instance) {
		if (instance == null) {
			instance = new RMVariableMap();
		}
		RMVariableMap.active = instance;
		return RMVariableMap.active;
	}
}
