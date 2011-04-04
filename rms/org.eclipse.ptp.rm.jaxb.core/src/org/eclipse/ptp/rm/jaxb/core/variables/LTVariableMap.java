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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;

public class LTVariableMap implements IVariableMap, IJAXBNonNLSConstants {
	private static LTVariableMap active;

	private final Map<String, String> variables;
	private boolean initialized;

	private LTVariableMap() {
		variables = Collections.synchronizedMap(new TreeMap<String, String>());
		initialized = false;
	}

	public void clear() {
		variables.clear();
		initialized = false;
	}

	public Map<String, String> getDiscovered() {
		throw new UnsupportedOperationException();
	}

	public Map<String, String> getSelected() {
		Map<String, String> map = new HashMap<String, String>();
		String selected = variables.get(SELECTED_ATTRIBUTES);
		if (selected != null && !ZEROSTR.equals(selected)) {
			String[] attr = selected.split(SP);
			for (String s : attr) {
				map.put(s, s);
			}
		}
		return map;
	}

	/**
	 * The rm: prefix points to the RMVariableResolver, lt: to the
	 * LTVariableResolver, so we substitute the latter and pass it off.
	 * 
	 * @param expression
	 * @return
	 */
	public String getString(String value) {
		try {
			value = value.replaceAll(VRM, VLT);
			return dereference(value);
		} catch (CoreException t) {
			JAXBCorePlugin.log(t);
		}
		return value;
	}

	public String getString(String jobId, String value) {
		return getString(value);
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void maybeOverwrite(String key1, String key2, ILaunchConfiguration configuration) throws CoreException {
		String value = null;
		String p = variables.get(key1);
		if (p != null) {
			value = p;
		}
		value = configuration.getAttribute(key2, value);
		variables.put(key1, value);
	}

	public void setAllSelected(Map<String, String> map) {
		StringBuffer buffer = new StringBuffer();
		for (String s : map.keySet()) {
			buffer.append(s).append(SP);
		}
		variables.put(SELECTED_ATTRIBUTES, buffer.toString().trim());
		System.out.println(SELECTED_ATTRIBUTES + SP + buffer.toString().trim());
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (String key : variables.keySet()) {
			buffer.append(key).append(EQ).append(variables.get(key)).append(LINE_SEP);
		}
		return buffer.toString();
	}

	private String dereference(String expression) throws CoreException {
		if (expression == null) {
			return null;
		}
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	public static LTVariableMap createInstance(RMVariableMap rmVars) {
		LTVariableMap ltMap = new LTVariableMap();
		rmVars.getFlattenedVariables(ltMap.variables);
		String selected = ltMap.getVariables().get(SELECTED_ATTRIBUTES);
		if (selected == null || ZEROSTR.equals(selected.trim())) {
			ltMap.setAllSelected(ltMap.variables);
		}
		return ltMap;
	}

	public synchronized static LTVariableMap getActiveInstance() {
		return active;
	}

	public synchronized static void setActiveInstance(LTVariableMap instance) {
		active = instance;
	}
}
