package org.eclipse.ptp.rm.jaxb.core.utils;

import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentVariable;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class EnvVarUtils implements IJAXBNonNLSConstants {

	private EnvVarUtils() {
	}

	public static void addVariable(EnvironmentVariable var, Map<String, String> env, RMVariableMap map) {
		String key = var.getValueFrom();
		String value = getValue(key, map);
		if (value != null && !ZEROSTR.equals(value)) {
			env.put(var.getVariableName(), value);
		}
	}

	public static void addVariable(EnvironmentVariable var, String syntax, StringBuffer buffer, RMVariableMap map) {
		String key = var.getValueFrom();
		String value = getValue(key, map);
		addVariable(var.getVariableName(), value, syntax, buffer);
	}

	public static void addVariable(String name, String value, String syntax, StringBuffer buffer) {
		if (value != null && !ZEROSTR.equals(value)) {
			if (SETENV.equals(syntax)) {
				setenv(name, value, buffer);
			} else if (EXPORT.equals(syntax)) {
				export(name, value, buffer);
			}
		}
	}

	public static String getValue(String key, RMVariableMap map) {
		String name = OPENVRM + key + CLOSVAL;
		return map.getString(name);
	}

	private static void export(String name, String value, StringBuffer buffer) {
		buffer.append(EXPORT).append(SP).append(name).append(EQ).append(QT).append(value).append(QT).append(REMOTE_LINE_SEP);
	}

	private static void setenv(String name, String value, StringBuffer buffer) {
		buffer.append(SETENV).append(SP).append(name).append(SP).append(QT).append(value).append(QT).append(REMOTE_LINE_SEP);
	}
}
