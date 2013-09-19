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
package org.eclipse.ptp.internal.rm.jaxb.control.core.variables;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.ems.core.EnvManagerRegistry;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.remote.core.IRemoteConnection;

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
	 * Checks for current valid attributes by examining the valid list for the current controller, excluding <code>null</code>.
	 * Attributes with zero length strings are considered valid (see bug 414308). Removes the rm unique id prefix.
	 * 
	 * @param config
	 * @return currently valid variables
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getValidAttributes(ILaunchConfiguration config) throws CoreException {
		String rmId = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
				JAXBControlConstants.TEMP);
		rmId += JAXBControlConstants.DOT;
		int len = rmId.length();

		Map<String, Object> attrs = config.getAttributes();
		Map<String, Object> current = new TreeMap<String, Object>();
		Map<String, Object> rmAttr = new HashMap<String, Object>();
		Set<String> include = new HashSet<String>();

		for (String name : attrs.keySet()) {
			Object value = attrs.get(name);
			if (value == null) {
				continue;
			}
			if (name.startsWith(rmId)) {
				name = name.substring(len);
				if (isFixedValid(name)) {
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

		for (String var : rmAttr.keySet()) {
			Object value = rmAttr.get(var);
			if (include.contains(var)) {
				current.put(var, value);
			}
		}
		return current;
	}

	/**
	 * Dynamic variables are not saved or loaded from the launch configuration. They are created when the controller is started and
	 * are fixed while the controller is running.
	 * 
	 * @param name
	 *            of attribute
	 * @return true if it is dynamic
	 */
	public static boolean isDynamic(String name) {
		return name.equals(IRemoteConnection.OS_ARCH_PROPERTY) || name.equals(IRemoteConnection.OS_NAME_PROPERTY)
				|| name.equals(IRemoteConnection.OS_VERSION_PROPERTY);
	}

	/**
	 * @param name
	 *            of attribute
	 * @return whether it is a ptp or debug
	 */
	public static boolean isExternal(String name) {
		return name.startsWith(JAXBControlConstants.DEBUG_PACKAGE) || name.startsWith(JAXBControlConstants.PTP_PACKAGE);
	}

	/**
	 * Standard attribute needed by control.
	 * 
	 * @param name
	 *            of attribute
	 * @return if it belongs to this group
	 */
	public static boolean isFixedValid(String name) {
		return name.startsWith(JAXBControlConstants.CONTROL_DOT) || name.equals(JAXBControlConstants.EXEC_PATH)
				|| name.equals(JAXBControlConstants.EXEC_DIR) || name.equals(JAXBControlConstants.PROG_ARGS)
				|| name.equals(JAXBControlConstants.DEBUGGER_EXEC_PATH) || name.equals(JAXBControlConstants.DEBUGGER_ARGS)
				|| name.equals(JAXBControlConstants.DEBUGGER_ID) || name.equals(JAXBControlConstants.DEBUGGER_LAUNCHER)
				|| name.equals(JAXBControlConstants.STDOUT_REMOTE_FILE) || name.equals(JAXBControlConstants.STDERR_REMOTE_FILE);
	}

	private final Map<String, AttributeType> variables;

	private final Map<String, AttributeType> discovered;

	private boolean initialized;
	/** Environment manager for this connection, or <code>null</code> */
	private IEnvManager envManager;
	/**
	 * A connection from which an environment management system can be detected at a later point in time, if necessary. Used when
	 * {@link #envManager} is <code>null</code>.
	 */
	private IRemoteConnection envManagerConnection;

	public RMVariableMap() {
		this.variables = Collections.synchronizedMap(new TreeMap<String, AttributeType>());
		this.discovered = Collections.synchronizedMap(new TreeMap<String, AttributeType>());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#get(java.lang.String)
	 */
	public AttributeType get(String name) {
		if (name == null) {
			return null;
		}
		AttributeType a = variables.get(name);
		if (a == null) {
			a = discovered.get(name);
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getAttributes()
	 */
	public Map<String, AttributeType> getAttributes() {
		return variables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getDefault(java.lang.String)
	 */
	public String getDefault(String name) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getDiscovered()
	 */
	public Map<String, AttributeType> getDiscovered() {
		return discovered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getEnvManager()
	 */
	public IEnvManager getEnvManager() {
		if (envManager == null && envManagerConnection != null) {
			envManager = EnvManagerRegistry.getEnvManager(new NullProgressMonitor(), envManagerConnection);
		}
		return envManager;
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
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getValue(java.lang.String)
	 */
	public Object getValue(String name) {
		AttributeType a = variables.get(name);
		if (a != null) {
			return a.getValue();
		}
		return null;
	}

	/**
	 * @return whether the map has been initialized from an XML resource manager configuration.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Only creates an attribute and adds it if value is not <code>null</code>.
	 * 
	 * @param name
	 *            of attribute to add
	 * @param value
	 *            of attribute to add
	 * @param visible
	 *            whether this attribute is to be made available through the user interface (Launch Tab)
	 */
	public void maybeAddAttribute(String name, Object value, boolean visible) {
		if (name == null) {
			return;
		}

		AttributeType a = get(name);
		if (a == null) {
			a = new AttributeType();
			a.setName(name);
			variables.put(name, a);
		}

		if (value != null) {
			a.setValue(value);
			a.setVisible(visible);
		}
	}

	/**
	 * Looks for an attribute in the configuration and uses it to create an attribute in the current environment. If the attribute
	 * is undefined or has a <code>null</code> value in the configuration, this will not overwrite a currently defined attribute in
	 * the environment. <br>
	 * <br>
	 * Delegates to {@link #maybeAddAttribute(String, AttributeType, boolean)}
	 * 
	 * @param key1
	 *            to map the attribute to in the environment
	 * @param key2
	 *            to search for the attribute in the configuration
	 * @param map
	 *            to search
	 * @throws CoreException
	 */
	public void overwrite(String key1, String key2, Map<String, Object> map) throws CoreException {
		Object value = map.get(key2);
		if (value != null) {
			maybeAddAttribute(key1, value, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#put(java.lang.String, java.lang.Object)
	 */
	public void put(String name, AttributeType value) {
		if (name == null) {
			return;
		}
		variables.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String name, Object value) {
		if (name == null || JAXBControlConstants.ZEROSTR.equals(name)) {
			return;
		}
		if (value != null) {
			AttributeType a = variables.get(name);
			if (a == null) {
				a = new AttributeType();
				a.setName(name);
				a.setValue(value);
				variables.put(name, a);
			}
			a.setValue(value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#remove(java.lang.String)
	 */
	public AttributeType remove(String name) {
		if (name == null) {
			return null;
		}
		AttributeType o = variables.remove(name);
		if (o == null) {
			o = discovered.remove(name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultValue) {
		// This map does not support default values
	}

	public void setEnvManagerFromConnection(IRemoteConnection connection) {
		this.envManager = null;
		this.envManagerConnection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#setInitialized(boolean)
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
}
