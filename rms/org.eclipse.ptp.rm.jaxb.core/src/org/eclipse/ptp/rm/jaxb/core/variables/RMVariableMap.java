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

public class RMVariableMap implements IJAXBNonNLSConstants {
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
			t.printStackTrace();
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
