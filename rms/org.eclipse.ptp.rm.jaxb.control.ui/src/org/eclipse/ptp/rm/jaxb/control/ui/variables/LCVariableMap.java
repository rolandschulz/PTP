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
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;

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
		String cdir = configuration.getAttribute(rmPrefix + JAXBControlConstants.CONTROL_WORKING_DIR_VAR,
				JAXBControlConstants.ZEROSTR);
		String dir = configuration.getAttribute(rmPrefix + JAXBControlConstants.DIRECTORY, JAXBControlConstants.ZEROSTR);
		if (dir == null || JAXBControlConstants.ZEROSTR.equals(dir)) {
			dir = (cdir == null ? JAXBControlConstants.ZEROSTR : cdir);
		}

		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DIRECTORY);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.PTP_DIRECTORY);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.EXEC_PATH);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.EXEC_DIR);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.PROG_ARGS);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_EXEC_PATH);
		configuration.removeAttribute(rmPrefix + JAXBControlConstants.DEBUGGER_ARGS);

		String attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, dir);
		if (!JAXBControlConstants.ZEROSTR.equals(attr)) {
			configuration.setAttribute(rmPrefix + JAXBControlConstants.DIRECTORY, attr);
		}
		String ptp_dir = JAXBControlConstants.ECLIPSESETTINGS;
		if (!JAXBControlConstants.ZEROSTR.equals(cdir)) {
			ptp_dir = new Path(cdir).append(JAXBControlConstants.ECLIPSESETTINGS).toString();
		}
		configuration.setAttribute(rmPrefix + JAXBControlConstants.PTP_DIRECTORY, ptp_dir);
		attr = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, JAXBControlConstants.ZEROSTR);
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
	}

	private final Map<String, Object> linkedTo;
	private final Map<String, Object> excluded;
	private final Map<String, Object> values;
	private final Map<String, String> defaultValues;
	private final Map<String, Object> temp;

	private final Set<String> hidden;

	private String rmPrefix;

	public LCVariableMap() {
		this.values = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.excluded = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.defaultValues = Collections.synchronizedMap(new TreeMap<String, String>());
		this.linkedTo = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.temp = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.hidden = new HashSet<String>();
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
			Object value = values.get(name);
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

	/**
	 * @param name
	 *            of tab or viewer for which to find qualifying widgets or checked rows
	 * @param statePrefix
	 *            the tag for the control state
	 * @return set of variable names in this control state
	 */
	public Set<String> forControlState(String name, String statePrefix) {
		Set<String> set = new TreeSet<String>();
		String state = (String) get(statePrefix + name);
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
	public Object get(String name) {
		if (name == null) {
			return null;
		}
		return values.get(rmPrefix + name);
	}

	/**
	 * @param viewerName
	 *            of viewer for which to find checked rows
	 * @return set of checked row model names
	 */
	public Set<String> getChecked(String viewerName) {
		return forControlState(viewerName, JAXBControlUIConstants.CHECKED_ATTRIBUTES);
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
	public Map<String, Object> getDiscovered() {
		return null;
	}

	/**
	 * @return hiddenDiscovered
	 */
	public Map<String, Object> getExcluded() {
		return excluded;
	}

	/**
	 * @return the set of properties and attributes marked not visible
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
			JAXBCorePlugin.log(t);
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
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#getVariables()
	 */
	public Map<String, Object> getVariables() {
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
		this.rmPrefix = rmId + JAXBUIConstants.DOT;
		for (String s : rmVars.getVariables().keySet()) {
			loadValues(s, rmVars.getVariables().get(s), false);
		}
		for (String s : rmVars.getDiscovered().keySet()) {
			loadValues(s, rmVars.getDiscovered().get(s), true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IVariableMap#put(java.lang.String, java.lang.Object)
	 */
	public void put(String name, Object value) {
		if (name == null || JAXBUIConstants.ZEROSTR.equals(name)) {
			return;
		}
		if (value != null) {
			values.put(rmPrefix + name, value);
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
		for (Iterator<String> key = values.keySet().iterator(); key.hasNext();) {
			String name = key.next();
			if (!name.startsWith(rmPrefix)) {
				key.remove();
			}
		}

		Map<?, ?> attributes = configuration.getAttributes();
		for (Object o : attributes.keySet()) {
			String key = (String) o;
			if (RMVariableMap.isExternal(key)) {
				values.put(key, attributes.get(key));
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
			Object o = linkedTo.get(name);
			String link = null;
			if (o instanceof PropertyType) {
				PropertyType p = (PropertyType) o;
				link = p.getLinkValueTo();
			} else if (o instanceof AttributeType) {
				AttributeType a = (AttributeType) o;
				link = a.getLinkValueTo();
			}
			if (link != null) {
				if (valid.contains(link) || RMVariableMap.isExternal(link) || RMVariableMap.isFixedValid(link)) {
					value = get(link);
				}
			}
			if (value == null || JAXBUIConstants.ZEROSTR.equals(value)) {
				value = defaultValues.get(name);
			}
			if (value == null) {
				value = JAXBUIConstants.ZEROSTR;
			}
			put(name, value);
		}
	}

	/**
	 * Not allowed on the LCVariableMap
	 */
	public Object remove(String name) {
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
			put(name, defaultv);
		}
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
			Object value = temp.get(var);
			if (JAXBUIConstants.ZEROSTR.equals(value)) {
				continue;
			}
			if (var != null) {
				if (var.startsWith(rmPrefix)) {
					var = var.substring(rmPrefix.length());
					if (valid.contains(var) || RMVariableMap.isFixedValid(var)) {
						put(var, value);
					}
				} else if (RMVariableMap.isExternal(var)) {
					values.put(var, value);
				}
			}
		}
	}

	/**
	 * Update the loaded map with the most recent values from the configuration.
	 * 
	 * @throws CoreException
	 */
	@SuppressWarnings({ "unchecked" })
	public void updateFromConfiguration(ILaunchConfiguration configuration) throws CoreException {
		Map<String, Object> attr = configuration.getAttributes();
		for (String key : attr.keySet()) {
			if (key.startsWith(rmPrefix) || RMVariableMap.isExternal(key)) {
				values.put(key, attr.get(key));
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
	 * If the value of the Property or Attribute is <code>null</code> and it has a defined default, the value is set to the default.
	 * 
	 * @param key
	 *            from the original RMVariableMap
	 * @param value
	 *            the Property or Attribute
	 * @param discovered
	 *            property was discovered at run time
	 * @throws Throwable
	 */
	private void loadValues(String key, Object value, boolean discovered) throws Throwable {
		String name = null;
		String defVal = null;
		boolean linked = false;
		boolean visible = true;
		Object o = null;
		if (value instanceof PropertyType) {
			PropertyType p = (PropertyType) value;
			name = p.getName();
			if (name == null) {
				return;
			}
			defVal = p.getDefault();
			visible = p.isVisible();
			if (!visible) {
				hidden.add(name);
				if (p.getLinkValueTo() != null) {
					linked = true;
					linkedTo.put(name, p);
				} else {
					o = p.getValue();
				}
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
			visible = ja.isVisible();
			if (!visible) {
				hidden.add(name);
				if (ja.getLinkValueTo() != null) {
					linked = true;
					linkedTo.put(name, ja);
				} else {
					o = ja.getValue();
				}
			} else {
				o = ja.getValue();
			}
		} else {
			throw new ArrayStoreException(Messages.IllegalVariableValueType + value);
		}

		if (!discovered) {
			defaultValues.put(name, defVal);
			if (!linked) {
				if (o == null) {
					setDefault(name, defVal);
				} else {
					put(name, o);
				}
			}
		} else {
			if (!visible) {
				hidden.add(name);
				excluded.put(name, o);
			} else {
				put(name, o);
			}
		}
	}
}
