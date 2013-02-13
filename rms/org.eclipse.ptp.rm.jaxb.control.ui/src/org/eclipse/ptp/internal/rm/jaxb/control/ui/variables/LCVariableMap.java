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
package org.eclipse.ptp.internal.rm.jaxb.control.ui.variables;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.variables.RMVariableMap;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;

/**
 * A wrapper for the LaunchConfiguration accessed through the IVariableMap interface.<br>
 * <br>
 * <br>
 * Unlike the RMVariableMap, the internal map here is largely flat in the sense that it holds only name-value pairs instead of the
 * Property or Attribute objects.<br>
 * <br>
 * When this map is loaded from its parent (see {@link #initialize(IVariableMap)}), the full set of Properties and Attributes are
 * maintained in a global map, which remains unaltered; a second, volatile map can be swapped in and out by the caller to view the
 * tab-specific environment.<br>
 * <br>
 * This object also maintains the default values defined from the parent in a separate map, and a map for invisible properties
 * (i.e., those not exported to widgets). Finally, it also holds aside linked properties for reevaluation at update.
 * 
 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class LCVariableMap implements IVariableMap {
	private static final Object monitor = new Object();

	/**
	 * Assures that properties set by the other tabs get included in this RM's property set.
	 * 
	 * @param rmPrefix
	 * @param configuration
	 * @throws CoreException
	 */
	public static void normalizeStandardProperties(String rmPrefix, ILaunchConfigurationWorkingCopy configuration)
			throws CoreException {
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.EXEC_PATH);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.EXEC_DIR);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.PROG_ARGS);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_EXEC_PATH);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_ARGS);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_ID);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_LAUNCHER);

		String attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,
				JAXBControlConstants.ZEROSTR);
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.EXEC_PATH, attr);
			attr = new Path(attr).removeLastSegments(1).toString();
			configuration.setAttribute(rmPrefix + JAXBControlConstants.EXEC_DIR, attr);
		}
		attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, JAXBControlConstants.ZEROSTR);
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.PROG_ARGS, attr);
		}
		attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
				JAXBControlConstants.ZEROSTR);
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_EXEC_PATH, attr);
		}
		attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS, JAXBControlConstants.ZEROSTR);
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_ARGS, attr);
		}
		attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, JAXBControlConstants.ZEROSTR);
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_ID, attr);
		}
		attr = configuration.getAttribute("org.eclipse.ptp.launch.DEBUGGER_LAUNCHER", JAXBControlConstants.ZEROSTR); //$NON-NLS-1$
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_LAUNCHER, attr);
		}
	}

	private IEnvManager envManager;
	private final Map<String, AttributeType> linkedTo;
	private final Map<String, Object> excluded;
	private final Map<String, AttributeType> values;
	private final Map<String, String> defaultValues;
	private final Map<String, AttributeType> temp;

	private final Set<String> hidden;

	private String rmPrefix;

	public LCVariableMap() {
		this.values = Collections.synchronizedMap(new TreeMap<String, AttributeType>());
		this.excluded = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.defaultValues = Collections.synchronizedMap(new TreeMap<String, String>());
		this.linkedTo = Collections.synchronizedMap(new TreeMap<String, AttributeType>());
		this.temp = Collections.synchronizedMap(new TreeMap<String, AttributeType>());
		this.hidden = new HashSet<String>();
		this.rmPrefix = JAXBUIConstants.ZEROSTR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#clear()
	 */
	public void clear() {
		values.clear();
		defaultValues.clear();
		linkedTo.clear();
		temp.clear();
		excluded.clear();
		hidden.clear();
	}

	/**
	 * Merge the attribute map on the configuration with the current (volatile) map.<br>
	 * <br>
	 * 
	 * Note that if any <code>null<code> values go into the working copy, they are not subsequently written to the original.  
	 * Thus to maintain consistency, we do no allow <code>null</code> values in the map.
	 * 
	 * @param configuration
	 *            working copy of Launch Tab's current configuration
	 * @throws CoreException
	 */
	@SuppressWarnings("rawtypes")
	public void flush(ILaunchConfigurationWorkingCopy configuration) throws CoreException {
		for (String name : values.keySet()) {
			if (!rmPrefix.equals(JAXBControlConstants.ZEROSTR)
					&& !RMVariableMap.isDynamic(name.replace(rmPrefix, JAXBControlConstants.ZEROSTR))) {
				Object value = getValueFromAttribute(name);
				if (value instanceof Boolean) {
					configuration.setAttribute(name, (Boolean) value);
				} else if (value instanceof Integer) {
					configuration.setAttribute(name, (Integer) value);
				} else if (value instanceof List) {
					configuration.setAttribute(name, (List) value);
				} else if (value instanceof Set) {
					configuration.setAttribute(name, (Set) value);
				} else if (value instanceof Map) {
					configuration.setAttribute(name, (Map) value);
				} else {
					configuration.setAttribute(name, (String) value);
				}
			}
		}
	}

	/**
	 * @param name
	 *            of tab or viewer for which to find qualifying widgets or checked rows
	 * @param statePrefix
	 *            the tag for the control state
	 * @return set of variable names in this control state
	 */
	public Set<String> forControlState(String name, String statePrefix) {
		Set<String> set = new TreeSet<String>();
		String state = (String) getValue(statePrefix + name);
		if (state != null) {
			String[] split = state.split(JAXBControlUIConstants.SP);
			for (String s : split) {
				set.add(s);
			}
		}
		return set;
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
		return values.get(rmPrefix + name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getVariables()
	 */
	public Map<String, AttributeType> getAttributes() {
		return values;
	}

	/**
	 * @param name
	 *            of widget, bound to a Property or Attribute
	 * @return default value of the Property or Attribute, or <code>null</code> if none
	 */
	public String getDefault(String name) {
		if (name == null) {
			return null;
		}
		return defaultValues.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getDiscovered()
	 */
	public Map<String, AttributeType> getDiscovered() {
		return null;
	}

	/**
	 * Get the environment manager for this map
	 * 
	 * @return environment manager
	 */
	public IEnvManager getEnvManager() {
		return envManager;
	}

	/**
	 * @return hiddenDiscovered
	 */
	public Map<String, Object> getExcluded() {
		return excluded;
	}

	/**
	 * @return the set of attributes marked not visible
	 */
	public Set<String> getHidden() {
		return hidden;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getString(java.lang.String)
	 */
	public String getString(String value) {
		/*
		 * The ${ptp_rm: prefix points to the RMVariableResolver, ${ptp_lc: to the LCVariableResolver, so we substitute the latter
		 * and pass off the substitution to the resolver for resolution.
		 */
		try {
			value = value.replaceAll(JAXBControlUIConstants.VRM, JAXBControlUIConstants.VLC);
			return dereference(value);
		} catch (CoreException t) {
			JAXBControlCorePlugin.log(t);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String jobId, String value) {
		return getString(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getValue(java.lang.String)
	 */
	public Object getValue(String name) {
		return getValueFromAttribute(rmPrefix + name);
	}

	private Object getValueFromAttribute(String name) {
		AttributeType a = values.get(name);
		if (a != null) {
			return a.getValue();
		}
		return null;
	}

	/**
	 * Initialize this map from the resource manager environment instance.
	 * 
	 * @param rmVars
	 *            resource manager environment map
	 * @param rmId
	 *            IPTPLaunchConfigurationConstants. ATTR_RESOURCE_MANAGER_UNIQUENAME
	 * @throws Throwable
	 */
	public void initialize(IVariableMap rmVars, String rmId) throws Throwable {
		clear();
		if (rmId != null) {
			rmPrefix = rmId + JAXBUIConstants.DOT;
		}
		for (String s : rmVars.getAttributes().keySet()) {
			loadValues(s, rmVars.getAttributes().get(s), false);
		}
		for (String s : rmVars.getDiscovered().keySet()) {
			loadValues(s, rmVars.getDiscovered().get(s), true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#put(java.lang.String, org.eclipse.ptp.rm.jaxb.core.data.AttributeType)
	 */
	public void put(String name, AttributeType attr) {
		if (name == null || JAXBUIConstants.ZEROSTR.equals(name)) {
			return;
		}
		values.put(rmPrefix + name, attr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String name, Object value) {
		if (name == null || JAXBUIConstants.ZEROSTR.equals(name)) {
			return;
		}
		putValueToAttribute(rmPrefix + name, value);
	}

	private void putValueToAttribute(String name, Object value) {
		if (value != null) {
			AttributeType a = values.get(name);
			if (a == null) {
				a = new AttributeType();
				a.setName(name);
				a.setValue(value);
				values.put(name, a);
			}
			a.setValue(value);
		}
	}

	/**
	 * Relink ptp, debug, directory, executable and arguments variables. Note that the externally defined variables do not get the
	 * rmId prefix.
	 * 
	 * @param configuration
	 *            current launch settings
	 * @throws CoreException
	 */
	public void relinkConfigurationProperties(ILaunchConfiguration configuration) throws CoreException {
		if (!rmPrefix.equals(JAXBControlConstants.ZEROSTR)) {
			for (Iterator<String> key = values.keySet().iterator(); key.hasNext();) {
				String name = key.next();
				if (!name.startsWith(rmPrefix)) {
					key.remove();
				}
			}
		}

		Map<?, ?> attributes = configuration.getAttributes();
		for (Object o : attributes.keySet()) {
			String key = (String) o;
			if (RMVariableMap.isExternal(key)) {
				putValueToAttribute(key, attributes.get(key));
			}
		}
	}

	/**
	 * Relink the hidden variables in the current environment. (Note that the presence of a symbolic link overrides the default
	 * value only if the linked value is not <code>null</code>).
	 * 
	 */
	public void relinkHidden(String controller) {
		Set<String> valid = forControlState(controller, JAXBUIConstants.VALID);
		for (String name : linkedTo.keySet()) {
			Object value = null;
			AttributeType a = linkedTo.get(name);
			String link = null;
			link = a.getLinkValueTo();
			if (link != null) {
				if (valid.contains(link) || RMVariableMap.isExternal(link) || RMVariableMap.isFixedValid(link)) {
					value = getValue(link);
				}
			}
			if (value == null || JAXBUIConstants.ZEROSTR.equals(value)) {
				value = defaultValues.get(name);
			}
			if (value == null) {
				value = JAXBUIConstants.ZEROSTR;
			}
			putValue(name, value);
		}
	}

	/**
	 * Not allowed on the LCVariableMap
	 */
	public AttributeType remove(String name) {
		return null;
	}

	/**
	 * Restores the value map from the temp map.
	 */
	public void restoreGlobal() {
		values.clear();
		values.putAll(temp);
		temp.clear();
	}

	/**
	 * Only here do we remove values.
	 * 
	 * @param name
	 * @param defaultv
	 */
	public void setDefault(String name, String defaultv) {
		if (defaultv == null) {
			values.remove(rmPrefix + name);
		} else {
			putValue(name, defaultv);
		}
	}

	/**
	 * Sets the {@link IEnvManager} which will be used to generate Bash commands from environment manager configuration strings.
	 * 
	 * @param envManager
	 */
	public void setEnvManager(IEnvManager envManager) {
		this.envManager = envManager;
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
	 * Sets the internal map to the currently valid variables.
	 */
	public void shiftToCurrent(String controller) {
		Set<String> valid = forControlState(controller, JAXBUIConstants.VALID);
		temp.putAll(values);
		values.clear();
		for (String var : temp.keySet()) {
			AttributeType a = temp.get(var);
			if (JAXBUIConstants.ZEROSTR.equals(a.getValue())) {
				continue;
			}
			if (!rmPrefix.equals(JAXBControlConstants.ZEROSTR) && var.startsWith(rmPrefix)) {
				var = var.substring(rmPrefix.length());
				if (valid.contains(var) || RMVariableMap.isFixedValid(var)) {
					putValue(var, a.getValue());
				}
			} else if (RMVariableMap.isExternal(var)) {
				putValueToAttribute(var, a.getValue());
			}
		}
	}

	/**
	 * Update the loaded map with the most recent values from the configuration.
	 * 
	 * @throws CoreException
	 */
	@SuppressWarnings({ "rawtypes" })
	public void updateFromConfiguration(ILaunchConfiguration configuration) throws CoreException {
		Map attr = configuration.getAttributes();
		for (Object keyObj : attr.keySet()) {
			if (keyObj instanceof String) {
				String key = (String) keyObj;
				if ((!rmPrefix.equals(JAXBControlConstants.ZEROSTR) && key.startsWith(rmPrefix) && !RMVariableMap.isDynamic(key
						.replace(rmPrefix, JAXBControlConstants.ZEROSTR))) || RMVariableMap.isExternal(key)) {
					putValueToAttribute(key, attr.get(key));
				}
			}
		}
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
			LCVariableResolver.setActive(this);
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
		}
	}

	/**
	 * If the value of the attribute is <code>null</code> and it has a defined default, the value is set to the default.
	 * 
	 * @param key
	 *            from the original RMVariableMap
	 * @param attr
	 *            the attribute
	 * @param discovered
	 *            attribute was discovered at run time
	 * @throws Throwable
	 */
	private void loadValues(String key, AttributeType attr, boolean discovered) throws Throwable {
		String name = null;
		String defVal = null;
		boolean linked = false;
		boolean visible = true;
		Object o = null;
		name = attr.getName();
		if (name == null) {
			return;
		}
		defVal = attr.getDefault();
		visible = attr.isVisible();
		if (!visible) {
			hidden.add(name);
			if (attr.getLinkValueTo() != null) {
				linked = true;
				linkedTo.put(name, attr);
			} else {
				o = attr.getValue();
			}
		} else {
			o = attr.getValue();
		}

		if (!discovered) {
			defaultValues.put(name, defVal);
			if (!linked) {
				if (o == null) {
					setDefault(name, defVal);
				} else {
					putValue(name, o);
				}
			}
		} else {
			if (!visible) {
				hidden.add(name);
				excluded.put(name, o);
			} else {
				putValue(name, o);
			}
		}
	}
}
