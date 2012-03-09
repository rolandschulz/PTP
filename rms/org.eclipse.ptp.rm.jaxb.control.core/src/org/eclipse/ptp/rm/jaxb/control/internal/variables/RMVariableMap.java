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
package org.eclipse.ptp.rm.jaxb.control.internal.variables;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;

/**
 * Abstraction representing all the Property and Attribute definitions associated with a resource manager configuration. This
 * provides an "environment" for dereferencing the resource manager configuration, such that the values of its Properties and
 * Attributes can reference other Properties and Attributes.<br>
 * <br>
 * 
 * There are two submaps: one contains all the Properties and Attributes defined in the XML configuration; a separate map contains
 * all the Properties and Attributes which are "discovered" at runtime (through tokenization of command output).
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class RMVariableMap implements IVariableMap {
	private static final Object monitor = new Object();

	/**
	 * Gets the new value of any attributes that may have changed as a result of the launch.
	 * 
	 * @param name
	 *            name of the attribute
	 * @param config
	 *            launch configuration
	 * @return new attribute value, or null if the attribute is not dynamic
	 * @throws CoreException
	 */
	public static Object getDynamicAttribute(String name, ILaunchConfiguration config) throws CoreException {
		if (name.equals(JAXBControlConstants.DEBUGGER_ARGS)) {
			return config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS, JAXBControlConstants.ZEROSTR);
		}
		return null;
	}

	/**
	 * Checks for current valid attributes by examining the valid list for the current controller, excluding <code>null</code> or
	 * 0-length string values. Removes the rm unique id prefix.
	 * 
	 * @param config
	 * @return currently valid variables
	 */
	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getValidAttributes(ILaunchConfiguration config) throws CoreException {
		String rmId = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
				JAXBControlConstants.TEMP);
		rmId += JAXBControlConstants.DOT;
		int len = rmId.length();

		Map<String, Object> attr = config.getAttributes();
		Map<Object, Object> current = new TreeMap<Object, Object>();
		Map<String, Object> rmAttr = new HashMap<String, Object>();
		Set<String> include = new HashSet<String>();

		for (String name : attr.keySet()) {
			Object value = attr.get(name);
			if (value == null || JAXBControlConstants.ZEROSTR.equals(value)) {
				continue;
			}
			if (name.startsWith(rmId)) {
				name = name.substring(len);
				if (isDynamicValid(name)) {
					current.put(name, getDynamicAttribute(name, config));
				} else if (isFixedValid(name)) {
					current.put(name, value);
				} else {
					rmAttr.put(name, value);
				}
			} else if (isExternal(name)) {
				current.put(name, value);
			}
		}

		String id = (String) rmAttr.get(JAXBCoreConstants.CURRENT_CONTROLLER);
		String valid = (String) rmAttr.get(JAXBCoreConstants.VALID + id);
		if (valid != null) {
			String[] split = valid.split(JAXBCoreConstants.SP);
			for (String s : split) {
				include.add(s);
			}
		}

		for (Object var : rmAttr.keySet()) {
			Object value = rmAttr.get(var);
			if (include.contains(var)) {
				current.put(var, value);
			}
		}
		return current;
	}

	/**
	 * @param name
	 *            or property or attribute
	 * @return whether it is a ptp or debug
	 */
	public static boolean isExternal(String name) {
		return name.startsWith(JAXBControlConstants.DEBUG_PACKAGE) || name.startsWith(JAXBControlConstants.PTP_PACKAGE);
	}

	/**
	 * Standard properties needed by control.
	 * 
	 * @param name
	 *            or property or attribute
	 * @return if it belongs to this group
	 */
	public static boolean isFixedValid(String name) {
		return name.startsWith(JAXBControlConstants.CONTROL_DOT) || name.equals(JAXBControlConstants.DIRECTORY)
				|| name.equals(JAXBControlConstants.EXEC_PATH) || name.equals(JAXBControlConstants.EXEC_DIR)
				|| name.equals(JAXBControlConstants.PROG_ARGS) || name.equals(JAXBControlConstants.DEBUGGER_EXEC_PATH)
				|| name.equals(JAXBControlConstants.DEBUGGER_ARGS) || name.equals(JAXBControlConstants.STDOUT_REMOTE_FILE)
				|| name.equals(JAXBControlConstants.STDERR_REMOTE_FILE) || name.equals(JAXBControlConstants.PTP_DIRECTORY);
	}

	public static boolean isDynamicValid(String name) {
		return name.equals(JAXBControlConstants.DEBUGGER_ARGS);
	}

	private IEnvManager envManager;
	private final Map<String, Object> variables;
	private final Map<String, Object> discovered;

	private boolean initialized;

	public RMVariableMap() {
		this.envManager = null;
		this.variables = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.discovered = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.initialized = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#clear()
	 */
	public void clear() {
		variables.clear();
		discovered.clear();
		initialized = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#get(java.lang.String)
	 */
	public Object get(String name) {
		if (name == null) {
			return null;
		}
		Object o = variables.get(name);
		if (o == null) {
			o = discovered.get(name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getDiscovered()
	 */
	public Map<String, Object> getDiscovered() {
		return discovered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getString(java.lang.String)
	 */
	public String getString(String value) {
		return getString(null, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String jobId, String value) {
		try {
			if (jobId != null) {
				value = value.replaceAll(JAXBControlConstants.JOB_ID_TAG, jobId);
			}
			return dereference(value);
		} catch (CoreException t) {
			JAXBControlCorePlugin.log(t);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getVariables()
	 */
	public Map<String, Object> getVariables() {
		return variables;
	}

	/**
	 * @return whether the map has been initialized from an XML resource manager configuration.
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
	 *            whether this property is to be made available through the user interface (Launch Tab)
	 */
	public void maybeAddProperty(String name, Object value, boolean visible) {
		if (name == null) {
			return;
		}

		Object o = get(name);
		PropertyType p = null;
		AttributeType a = null;
		if (o == null && value != null) {
			p = new PropertyType();
			variables.put(name, p);
		} else if (o instanceof PropertyType) {
			if (value != null) {
				p = (PropertyType) o;
			} else {
				remove(name);
			}
		} else if (o instanceof AttributeType) {
			if (value != null) {
				a = (AttributeType) o;
			} else {
				remove(name);
			}
		}

		if (p != null) {
			p.setName(name);
			p.setValue(value);
			p.setVisible(visible);
		} else if (a != null) {
			a.setName(name);
			a.setValue(value);
			a.setVisible(visible);
		}
	}

	/**
	 * Looks for property in the configuration and uses it to create a Property in the current environment. If the property is
	 * undefined or has a <code>null</code> value in the configuration, this will not overwrite a currently defined property in the
	 * environment. <br>
	 * <br>
	 * Delegates to {@link #maybeAddProperty(String, Object, boolean)}
	 * 
	 * @param key1
	 *            to map the property to in the environemnt
	 * @param key2
	 *            to search for the property in the configuration
	 * @param map
	 *            to search
	 * @throws CoreException
	 */
	public void overwrite(String key1, String key2, Map<String, Object> map) throws CoreException {
		maybeAddProperty(key1, map.get(key2), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#put(java.lang.String, java.lang.Object)
	 */
	public void put(String name, Object value) {
		if (name == null) {
			return;
		}
		variables.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#remove(java.lang.String)
	 */
	public Object remove(String name) {
		if (name == null) {
			return null;
		}
		Object o = variables.remove(name);
		if (o == null) {
			o = discovered.remove(name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#setInitialized(boolean)
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Calls the string substitution method on the variable manager. Under synchronization, sets the variable resolver's map
	 * reference to this instance.
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

	/**
	 * Sets the {@link IEnvManager} which will be used to generate Bash commands from environment manager configuration strings.
	 * @param envManager
	 */
	public void setEnvManager(IEnvManager envManager) {
		this.envManager = envManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#convertEngMgmtConfigString(java.lang.String)
	 */
	public String convertEngMgmtConfigString(String string) {
		assert EnvManagerConfigString.isEnvMgmtConfigString(string);
		if (envManager == null) {
			return ""; //$NON-NLS-1$
		} else {
			return envManager.getBashConcatenation("\n", false, new EnvManagerConfigString(string), null); //$NON-NLS-1$
		}
	}
}
