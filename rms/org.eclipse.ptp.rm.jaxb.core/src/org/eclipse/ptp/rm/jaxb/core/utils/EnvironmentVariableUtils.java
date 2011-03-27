/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.utils;

import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.NameValuePair;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class EnvironmentVariableUtils implements IJAXBNonNLSConstants {

	private EnvironmentVariableUtils() {
	}

	public static void addVariable(String uuid, NameValuePair var, Map<String, String> env, RMVariableMap map) {
		String key = var.getName();
		String value = getValue(uuid, key, map);
		if (value != null && !ZEROSTR.equals(value)) {
			env.put(var.getName(), value);
		}
	}

	public static void addVariable(String name, String value, String directive, StringBuffer buffer) {
		if (value != null && !ZEROSTR.equals(value)) {
			if (SETENV.equals(getSyntax(directive))) {
				setenv(name, value, buffer);
			} else if (EXPORT.equals(getSyntax(directive))) {
				export(name, value, buffer);
			}
		}
	}

	public static String getValue(String uuid, String key, RMVariableMap map) {
		String name = OPENVRM + key + CLOSVAL;
		return map.getString(uuid, name);
	}

	private static void export(String name, String value, StringBuffer buffer) {
		buffer.append(EXPORT).append(SP).append(name).append(EQ).append(QT).append(value).append(QT).append(REMOTE_LINE_SEP);
	}

	private static Object getSyntax(String directive) {
		if (directive.indexOf(CSH) >= 0) {
			return SETENV;
		}
		return EXPORT;
	}

	private static void setenv(String name, String value, StringBuffer buffer) {
		buffer.append(SETENV).append(SP).append(name).append(SP).append(QT).append(value).append(QT).append(REMOTE_LINE_SEP);
	}
}
