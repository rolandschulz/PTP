package org.eclipse.ptp.rm.jaxb.core.variables;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

/**
 * A wrapper for the LaunchConfiguration accessed through the IVariableMap
 * interface.
 */
public class LCVariableMap implements IVariableMap, IJAXBNonNLSConstants {

	private static LCVariableMap active;

	private Map<String, Object> globalValues;
	private Map<String, Object> values;
	private final Map<String, String> defaultValues;
	private final Map<String, String> selected;
	private boolean initialized;

	private LCVariableMap() {
		this.values = Collections.synchronizedMap(new TreeMap<String, Object>());
		this.defaultValues = Collections.synchronizedMap(new TreeMap<String, String>());
		this.selected = Collections.synchronizedMap(new TreeMap<String, String>());
		this.initialized = false;
	}

	public Object get(String name) {
		return values.get(name);
	}

	public String getDefault(String name) {
		return defaultValues.get(name);
	}

	/**
	 * The rm: prefix points to the RMVariableResolver, lc: to the
	 * LCVariableResolver, so we substitute the latter and pass it off.
	 * 
	 * @param expression
	 * @return
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

	public String getString(String jobId, String value) {
		return getString(value);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isSelected(String name) {
		return selected.containsKey(name);
	}

	public void put(String name, Object value) {
		values.put(name, value);
	}

	public Object remove(String name) {
		return values.remove(name);
	}

	public void restoreGlobal() {
		values = globalValues;
	}

	public void saveToConfiguration(ILaunchConfigurationWorkingCopy configuration) throws CoreException {
		configuration.setAttributes(values);
		configuration.doSave();
	}

	public Map<String, Object> selectVariables(List<String> keys) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (String k : keys) {
			if (values.containsKey(k)) {
				m.put(k, values.get(k));
			}
		}
		return m;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public Map<String, Object> swapVariables(Map<String, Object> newV) throws CoreException {
		Map<String, Object> oldV = values;
		values = newV;
		return oldV;
	}

	private String dereference(String expression) throws CoreException {
		if (expression == null) {
			return null;
		}
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	private void loadValues(RMVariableMap rmVars) throws Throwable {
		for (String s : rmVars.getVariables().keySet()) {
			loadValues(s, rmVars.getVariables().get(s));
		}
		for (String s : rmVars.getDiscovered().keySet()) {
			loadValues(s, rmVars.getDiscovered().get(s));
		}
		globalValues = values;
	}

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

	private void setSelected() throws CoreException {
		String selected = (String) get(SELECTED_ATTRIBUTES);
		if (selected == null || ZEROSTR.equals(selected)) {
			StringBuffer buffer = new StringBuffer();
			for (Object s : values.keySet()) {
				buffer.append(s).append(SP);
			}
			values.put(SELECTED_ATTRIBUTES, buffer.toString().trim());
		}
	}

	public static LCVariableMap createInstance(RMVariableMap rmVars) throws Throwable {
		LCVariableMap lcMap = new LCVariableMap();
		lcMap.setSelected();
		lcMap.loadValues(rmVars);
		return lcMap;
	}

	public synchronized static LCVariableMap getActiveInstance() {
		return active;
	}

	public synchronized static void setActiveInstance(LCVariableMap instance) {
		active = instance;
	}
}
