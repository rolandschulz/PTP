package org.eclipse.ptp.rm.jaxb.core.variables;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

public class RMVariableMap implements IJAXBNonNLSConstants {
	private static RMVariableMap singleton;

	private final Map<String, Object> variables;
	private final Map<String, Object> discovered;

	private RMVariableMap() {
		variables = Collections.synchronizedMap(new TreeMap<String, Object>());
		discovered = Collections.synchronizedMap(new TreeMap<String, Object>());
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
	public String getString(String value) {
		try {
			return dereference(value.toString());
		} catch (CoreException t) {
			t.printStackTrace();
		}
		return value;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	private String dereference(String expression) throws CoreException {
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	public synchronized static RMVariableMap getInstance() {
		if (singleton == null) {
			singleton = new RMVariableMap();
		}
		return singleton;
	}
}
