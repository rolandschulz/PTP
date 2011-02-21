package org.eclipse.ptp.rm.jaxb.core.variables;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;

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

	public Map<String, Object> getVariables() {
		return variables;
	}

	public void print() {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Map.Entry<String, Object>> i = variables.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Object> e = i.next();
			Object o = e.getValue();
			if (o instanceof JobAttribute) {
				JobAttribute ja = (JobAttribute) o;
				buffer.append(ja.getId()).append(PD).append(ja.getName()).append(PD).append(ja.getType()).append(PD)
						.append(ja.getDefault()).append(PD).append(ja.getChoice()).append(PD).append(ja.getMax()).append(PD)
						.append(ja.getMin()).append(PD).append(ja.getValidator()).append(PD).append(ja.getDescription()).append(PD)
						.append(ja.getTooltip()).append(LINE_SEP);

			} else
				buffer.append(e.getKey()).append(SP).append(e.getValue()).append(LINE_SEP);
		}
		System.out.println(buffer);
	}

	/**
	 * Performs the substitution on the string. rm: prefix points to the
	 * RMVariableResolver.
	 * 
	 * @param expression
	 * @return
	 * @throws CoreException
	 */
	public static String dereference(String expression) throws CoreException {
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
	}

	public synchronized static RMVariableMap getInstance() {
		if (singleton == null)
			singleton = new RMVariableMap();
		return singleton;
	}
}
