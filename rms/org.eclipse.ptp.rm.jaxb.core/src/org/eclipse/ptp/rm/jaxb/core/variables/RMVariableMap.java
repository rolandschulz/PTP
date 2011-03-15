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
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

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

	public Map<String, Object> getDiscovered() {
		return discovered;
	}

	public void getFlattenedDefaults(Map<String, String> defaults) {
		for (String s : variables.keySet()) {
			getDefault(s, variables.get(s), defaults);
		}

	}

	/**
	 * A flat name=value map of only the configurable attributes and properties.
	 * 
	 * @param flat
	 */
	public void getFlattenedDiscovered(Map<String, String> flat) {
		for (String s : discovered.keySet()) {
			getFlattened(s, discovered.get(s), flat);
		}
	}

	/**
	 * A flat name=value map of only the configurable attributes and properties.
	 * 
	 * @param flat
	 */
	public void getFlattenedVariables(Map<String, String> flat) {
		for (String s : variables.keySet()) {
			getFlattened(s, variables.get(s), flat);
		}
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

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	private String dereference(String expression) throws CoreException {
		if (expression == null) {
			return null;
		}
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	private void getDefault(String s, Object object, Map<String, String> defaults) {
		if (object instanceof JobAttribute) {
			JobAttribute ja = (JobAttribute) object;
			String defaultValue = ja.getDefault();
			if (defaultValue != null) {
				defaults.put(ja.getName(), defaultValue);
			}
		} else if (object instanceof Property) {
			Property p = (Property) object;
			String defaultValue = p.getDefault();
			if (defaultValue != null) {
				defaults.put(p.getName(), defaultValue);
			}
		}
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

	/*
	 * It is assumed that the caller of the flattened map does not need
	 * properties or attributes with complex values like lists or maps. Hence we
	 * simply cast the value to a string.
	 */
	private static void getFlattened(String key, Object value, Map<String, String> flat) throws ArrayStoreException {
		if (value instanceof Property) {
			Property p = (Property) value;
			Object o = p.getValue();
			String s = null;
			if (o != null) {
				s = o.toString();
			}
			flat.put(p.getName(), s);
		} else if (value instanceof JobAttribute) {
			JobAttribute ja = (JobAttribute) value;
			Object o = ja.getValue();
			String s = null;
			if (o != null) {
				s = o.toString();
			}
			flat.put(ja.getName(), s);
		} else if (value == null) {
			flat.put(key, null);
		} else {
			throw new ArrayStoreException(Messages.IllegalVariableValueType + value.getClass());
		}
	}
}
