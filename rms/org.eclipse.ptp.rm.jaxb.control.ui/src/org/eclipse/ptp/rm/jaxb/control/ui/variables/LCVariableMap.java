/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.variables;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;

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
 * When this map is loaded from its parent (see
 * {@link #initialize(IVariableMap)}), the full set of Properties and Attributes
 * are maintained in a global map, which remains unaltered; a second, volatile
 * map can be swapped in and out by the caller (usually subsets of the global
 * map based on the specific tab doing the calling).<br>
 * <br>
 * This object also maintains the default values defined from the parent in a
 * separate map, and a map for invisible properties (i.e., those not exported to
 * widgets). Finally, it also searches for and parses into an index the
 * currently checked values (from checkbox tables or trees). This index is used
 * when determining whether to null out the current value of a Property or
 * Attribute in the current (non-global) variable map.
 * 
 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap
 * 
 * @author arossi
 */
public class LCVariableMap implements IVariableMap {
	private static final Object monitor = new Object();

	private Map<String, Object> globalValues;
	private final Map<String, Object> hidden;
	private Map<String, Object> values;
	private final Map<String, String> defaultValues;

	public LCVariableMap() {
		this.values = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.defaultValues = Collections.synchronizedMap(new TreeMap<String, String>());
		this.hidden = Collections.synchronizedMap(new TreeMap<String, Object>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#clear()
	 */
	public void clear() {
		if (globalValues != null) {
			globalValues.clear();
		}
		if (values != null) {
			values.clear();
		}
		defaultValues.clear();
		hidden.clear();
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
	 * @param viewerName
	 *            of viewer for which to find checked rows
	 * @return map of checked row model names
	 */
	public Map<String, String> getChecked(String viewerName) {
		Map<String, String> m = new HashMap<String, String>();
		String checked = (String) values.get(JAXBControlUIConstants.CHECKED_ATTRIBUTES + viewerName);
		if (checked != null) {
			String[] split = checked.split(JAXBControlUIConstants.SP);
			for (String s : split) {
				m.put(s, s);
			}
		}
		return m;
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

	/*
	 * Unsupported (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getDiscovered()
	 */
	public Map<String, Object> getDiscovered() {
		return null;
	}

	/**
	 * Determine the value of a hidden property. Note that the presence of a
	 * symbolic link overrides the value which in turn overrides the default
	 * value.
	 * 
	 * @param name
	 * @param current
	 * @return
	 */
	public Object getHiddenValue(String name, Map<String, Object> current) {
		Object value = null;
		Object o = hidden.get(name);
		String link = null;
		if (o instanceof PropertyType) {
			PropertyType p = (PropertyType) o;
			value = p.getValue();
			link = p.getLinkValueTo();
		} else if (o instanceof AttributeType) {
			AttributeType a = (AttributeType) o;
			value = a.getValue();
			link = a.getLinkValueTo();
		}
		if (link != null) {
			Object linked = current.get(link);
			if (linked != null && !JAXBUIConstants.ZEROSTR.equals(linked)) {
				value = linked;
			}
		}
		if (value == null || JAXBUIConstants.ZEROSTR.equals(value)) {
			value = defaultValues.get(name);
		}
		return value;
	}

	/**
	 * Ensures that non-JAXB-spacific attributes and hidden variables remain in
	 * the configuration during replace/refresh.
	 * 
	 * @param configuration
	 *            current launch settings
	 * @return map of org.eclipse.debug and org.eclipse.ptp attributes
	 * @throws CoreException
	 */
	public Map<String, Object> getStandardConfigurationProperties(ILaunchConfiguration configuration, Map<String, Object> current)
			throws CoreException {
		Map<String, Object> standard = new TreeMap<String, Object>();
		for (String name : hidden.keySet()) {
			Object value = getHiddenValue(name, current);
			if (value != null) {
				standard.put(name, value);
			}
		}
		Map<?, ?> attributes = configuration.getAttributes();
		for (Object o : attributes.keySet()) {
			String key = (String) o;
			if (key.startsWith(JAXBControlConstants.DEBUG_PACKAGE) || key.startsWith(JAXBControlConstants.PTP_PACKAGE)) {
				standard.put(key, attributes.get(key));
			}
		}
		standard.put(
				JAXBControlConstants.DIRECTORY,
				configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR,
						(String) standard.get(JAXBControlConstants.CONTROL_WORKING_DIR_VAR)));
		standard.put(JAXBControlConstants.EXEC_PATH,
				configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, JAXBControlConstants.ZEROSTR));
		standard.put(JAXBControlConstants.PROG_ARGS,
				configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, JAXBControlConstants.ZEROSTR));
		return standard;
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
			value = value.replaceAll(JAXBControlUIConstants.VRM, JAXBControlUIConstants.VLC);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getVariables()
	 */
	public Map<String, Object> getVariables() {
		return values;
	}

	/**
	 * Initialize this map from the resource manager environment instance.
	 * 
	 * @param rmVars
	 *            resource manager environment map
	 * @throws Throwable
	 */
	public void initialize(IVariableMap rmVars) throws Throwable {
		clear();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#setInitialized(boolean)
	 */
	public void setInitialized(boolean initialized) {
		// does nothing
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
			LCVariableResolver.setActive(this);
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
		}
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
		boolean visible = true;
		Object o = null;
		if (value instanceof PropertyType) {
			PropertyType p = (PropertyType) value;
			name = p.getName();
			if (name == null) {
				return;
			}
			defVal = p.getDefault();
			if (!p.isVisible()) {
				hidden.put(name, p);
			} else {
				o = p.getValue();
			}
		} else if (value instanceof AttributeType) {
			AttributeType ja = (AttributeType) value;
			name = ja.getName();
			if (name == null) {
				return;
			}
			defVal = ja.getDefault();
			if (!ja.isVisible()) {
				hidden.put(name, ja);
			} else {
				o = ja.getValue();
				String status = ja.getStatus();
				put(name + JAXBControlConstants.PD + JAXBControlConstants.STATUS, status);
			}
		} else {
			throw new ArrayStoreException(Messages.IllegalVariableValueType + value);
		}
		defaultValues.put(name, defVal);
		if (visible) {
			if (o != null) {
				strVal = String.valueOf(o);
			}
			if (strVal == null) {
				strVal = defVal;
			}
			put(name, strVal);
		}
	}
}
