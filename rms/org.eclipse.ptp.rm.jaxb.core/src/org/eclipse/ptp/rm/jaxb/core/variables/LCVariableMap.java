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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

/**
 * A wrapper for the LaunchConfiguration accessed through the IVariableMap
 * interface.<br>
 * <br>
 * Note that this map is <b>not</b> tightly coupled to a given launch
 * configuration instance. When the map is flushed to the configuration, its
 * internal map simply replaces the attribute map on the configuration working
 * copy. <br>
 * <br>
 * Unlike the RMVariableMap, the internal map here is largely flat in the sense
 * that it holds only name-value primitive wrappers or strings instead of the
 * Property or Attribute objects (an exception is the "environment" Map passed
 * in to the configuration by the Environment Tab).<br>
 * <br>
 * When this map is loaded from its RMVariableMap parent (see
 * {@link #loadValues(RMVariableMap)}), the full set of Properties and
 * Attributes are maintained in a global map, which remains unaltered; a second,
 * volatile map can be swapped in and out by the caller (usually subsets of the
 * global map based on the specific tab doing the calling).<br>
 * <br>
 * This object also maintains the default values defined from the parent in a
 * separate map. Finally, it also searches for and parses into an index the
 * currently checked values (from checkbox tables or trees). This index is used
 * when determining whether to null out the current value of a Property or
 * Attribute in the current (non-global) variable map.
 * 
 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap
 * @see org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap
 * 
 * @author arossi
 */
public class LCVariableMap implements IVariableMap, IJAXBNonNLSConstants {

	private static LCVariableMap active;

	private Map<String, Object> globalValues;
	private Map<String, Object> values;
	private final Map<String, String> defaultValues;
	private final Map<String, String> checked;
	private final boolean initialized;

	private LCVariableMap() {
		this.values = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.defaultValues = Collections.synchronizedMap(new TreeMap<String, String>());
		this.checked = Collections.synchronizedMap(new TreeMap<String, String>());
		this.initialized = false;
	}

	/**
	 * @param name
	 *            of widget, bound to a Property or Attribute
	 * @return value of the Property or Attribute, or <code>null</code> if none
	 */
	public Object get(String name) {
		return values.get(name);
	}

	/**
	 * @param name
	 *            of widget, bound to a Property or Attribute
	 * @return default value of the Property or Attribute, or <code>null</code>
	 *         if none
	 */
	public String getDefault(String name) {
		return defaultValues.get(name);
	}

	/**
	 * The ${rm: prefix points to the RMVariableResolver, ${lc: to the
	 * LCVariableResolver, so we substitute the latter and pass off the
	 * substitution to the resolver for resolution.
	 * 
	 * @param value
	 *            expression to be resolved
	 * @return resolved expression
	 */
	public String getString(String value) {
		try {
			value = value.replaceAll(VRM, VLC);
			return dereference(value);
		} catch (CoreException t) {
			JAXBCorePlugin.log(t);
		}
		return value;
	}

	/**
	 * Interface method. Only called by {@link #getString(String)}.
	 * 
	 * @param jobId
	 *            is irrelevant
	 * @param value
	 *            expression to be resolved
	 * @return resolved expression
	 */
	public String getString(String jobId, String value) {
		return getString(value);
	}

	/**
	 * @param name
	 *            of widget, bound to a Property or Attribute
	 * @return if this is a checkbox element, whether it is checked
	 */
	public boolean isChecked(String name) {
		return checked.containsKey(name);
	}

	/**
	 * @return whether the internal maps have been loaded
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param name
	 *            of widget, bound to a Property or Attribute
	 * @param value
	 *            of Property or Attribute
	 */
	public void put(String name, Object value) {
		values.put(name, value);
	}

	/**
	 * 
	 * @param name
	 *            of widget, bound to a Property or Attribute
	 * @return value of Property or Attribute
	 */
	public Object remove(String name) {
		return values.remove(name);
	}

	/**
	 * Exchange the current volatile map for the one passed in.
	 * 
	 * @param newV
	 *            map to replace current
	 * @return current map
	 * @throws CoreException
	 */
	public Map<String, Object> swapVariables(Map<String, Object> newV) throws CoreException {
		Map<String, Object> oldV = values;
		values = newV;
		return oldV;
	}

	/**
	 * Set the volatile map to the original global map, but first update the
	 * latter with the most recent values from the configuration.
	 * 
	 * @throws CoreException
	 */
	@SuppressWarnings("rawtypes")
	public void updateGlobal(ILaunchConfiguration configuration) throws CoreException {
		Map attr = configuration.getAttributes();
		for (Object k : attr.keySet()) {
			Object val = attr.get(k);
			if (val != null) {
				globalValues.put((String) k, attr.get(k));
			}
		}
		values = globalValues;
	}

	/**
	 * Replace the attribute map on the configuration with the current
	 * (volatile) map.
	 * 
	 * @param configuration
	 *            working copy of Launch Tab's current configuration
	 * @throws CoreException
	 */
	public void writeToConfiguration(ILaunchConfigurationWorkingCopy configuration) throws CoreException {
		configuration.setAttributes(values);
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
	 * Initialize this map from the resource manager environment instance.
	 * 
	 * @param rmVars
	 *            resource manager environment map
	 * @throws Throwable
	 */
	private void loadValues(RMVariableMap rmVars) throws Throwable {
		for (String s : rmVars.getVariables().keySet()) {
			loadValues(s, rmVars.getVariables().get(s));
		}
		for (String s : rmVars.getDiscovered().keySet()) {
			loadValues(s, rmVars.getDiscovered().get(s));
		}
		globalValues = values;
		/*
		 * this map will be set from the tab's local map
		 */
		values = null;
	}

	/**
	 * If the value of the Property or Attribute is <code>null</code> and it has
	 * a defined default, the value is set to the default.
	 * 
	 * @param key
	 *            from the original RMVariableMap
	 * @param value
	 *            the Property or Attribute
	 * @throws Throwable
	 */
	private void loadValues(String key, Object value) throws Throwable {
		String name = null;
		String defVal = null;
		String strVal = null;
		Object o = null;
		if (value instanceof Property) {
			Property p = (Property) value;
			name = p.getName();
			defVal = p.getDefault();
			o = p.getValue();
		} else if (value instanceof Attribute) {
			Attribute ja = (Attribute) value;
			name = ja.getName();
			defVal = ja.getDefault();
			o = ja.getValue();
			String status = ja.getStatus();
			put(name + PD + STATUS, status);
		} else {
			throw new ArrayStoreException(Messages.IllegalVariableValueType + value.getClass());
		}
		defaultValues.put(name, defVal);
		if (o != null) {
			strVal = String.valueOf(o);
		}
		if (strVal == null) {
			strVal = defVal;
		}
		put(name, strVal);
	}

	/**
	 * The selected index is created from the SELECTED_ATTRIBUTES property, if
	 * it is defined.
	 * 
	 * @throws CoreException
	 */
	private void setChecked() throws CoreException {
		String checked = (String) get(CHECKED_ATTRIBUTES);
		if (checked == null || ZEROSTR.equals(checked)) {
			StringBuffer buffer = new StringBuffer();
			for (Object s : values.keySet()) {
				buffer.append(s).append(SP);
			}
			values.put(CHECKED_ATTRIBUTES, buffer.toString().trim());
		}
	}

	/**
	 * Creates an instance to be associated with a given Launch Tab.
	 * 
	 * @param rmVars
	 *            environement map for the associated resource manager
	 * @return the LaunchTab environment map
	 * @throws Throwable
	 */
	public static LCVariableMap createInstance(RMVariableMap rmVars) throws Throwable {
		LCVariableMap lcMap = new LCVariableMap();
		lcMap.setChecked();
		lcMap.loadValues(rmVars);
		return lcMap;
	}

	/**
	 * @return the currently active map
	 */
	public synchronized static LCVariableMap getActiveInstance() {
		return active;
	}

	/**
	 * Ensures that non-JAXB-spacific attributes remain in the configuration
	 * during replace/refresh.
	 * 
	 * @param configuration
	 *            current launch settings
	 * @return map of org.eclipse.debug and org.eclipse.ptp attributes
	 * @throws CoreException
	 */
	public static Map<String, Object> getStandardConfigurationProperties(ILaunchConfiguration configuration) throws CoreException {
		Map<String, Object> standard = new TreeMap<String, Object>();
		Map<?, ?> attributes = configuration.getAttributes();
		for (Object o : attributes.keySet()) {
			String key = (String) o;
			if (key.startsWith(DEBUG_PACKAGE) || key.startsWith(PTP_PACKAGE)) {
				standard.put(key, attributes.get(key));
			}
		}
		standard.put(DIRECTORY, configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, ZEROSTR));
		standard.put(EXEC_PATH, configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, ZEROSTR));
		standard.put(PROG_ARGS, configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, ZEROSTR));
		return standard;
	}

	/**
	 * @param instance
	 *            to be exported as the currently active map
	 */
	public synchronized static void setActiveInstance(LCVariableMap instance) {
		active = instance;
	}
}
