/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.utils;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.data.ArgImpl;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentType;

/**
 * Convenience methods for handling environment object (the JAXB NameValuePair).
 * 
 * @author arossi
 * 
 */
public class EnvironmentVariableUtils {

	/**
	 * Add variable to env after resolving against the resource-manager environment (used by the CommandJob).
	 * 
	 * @param uuid
	 *            an internal or resource-specific job id (can be <code>null</code>)
	 * @param var
	 *            environment name-value pair
	 * @param env
	 *            the environment map to which to add the pair
	 * @param map
	 *            resource manager active environment map
	 */
	public static void addVariable(String uuid, EnvironmentType var, Map<String, String> env, IVariableMap map) {
		String key = var.getValue();
		String value = null;
		if (key == null) {
			List<ArgType> args = var.getArg();
			if (args != null) {
				value = ArgImpl.toString(uuid, var.getArg(), map);
			}
		} else {
			value = map.getString(uuid, key);
		}
		if (value != null && !JAXBControlConstants.ZEROSTR.equals(value)) {
			env.put(var.getName(), value);
		}
	}

	/**
	 * Add variable to buffer after resolving against the resource-manager environment (used by the ScriptHandler).
	 * 
	 * @param name
	 *            name of variable
	 * @param value
	 *            value of variable
	 * @param directive
	 *            first line of shell script
	 * @param buffer
	 *            running contents of the script being generated
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.runnable.ScriptHandler#composeScript(IProgressMonitor)
	 */
	public static void addVariable(String name, String value, String directive, StringBuffer buffer) {
		if (value != null && !JAXBControlConstants.ZEROSTR.equals(value)) {
			if (JAXBControlConstants.SETENV.equals(getSyntax(directive))) {
				setenv(name, value, buffer);
			} else if (JAXBControlConstants.EXPORT.equals(getSyntax(directive))) {
				export(name, value, buffer);
			}
		}
	}

	/**
	 * For debugging purposes.
	 * 
	 * @param map
	 * @return string of n=v lines
	 */
	public static String toString(IVariableMap map) {
		StringBuffer b = new StringBuffer();
		for (AttributeType a : map.getAttributes().values()) {
			b.append(a.getName()).append(JAXBControlConstants.EQ).append(a.getValue()).append(JAXBControlConstants.LINE_SEP);
		}

		return b.toString();
	}

	/**
	 * Construct the bash-shell export command.
	 * 
	 * @param name
	 *            of variable
	 * @param value
	 *            of variable
	 * @param buffer
	 *            running contents of the script being generated
	 */
	private static void export(String name, String value, StringBuffer buffer) {
		buffer.append(JAXBControlConstants.EXPORT).append(JAXBControlConstants.SP).append(name).append(JAXBControlConstants.EQ)
				.append(JAXBControlConstants.QT).append(value).append(JAXBControlConstants.QT)
				.append(JAXBControlConstants.REMOTE_LINE_SEP);
	}

	/**
	 * Determines what syntax to use for script environment variable definition based on the first line of the script.
	 * 
	 * @param directive
	 *            first line of shell script
	 * @return syntax type.
	 */
	private static String getSyntax(String directive) {
		if (directive.indexOf(JAXBControlConstants.CSH) >= 0) {
			return JAXBControlConstants.SETENV;
		}
		return JAXBControlConstants.EXPORT;
	}

	/**
	 * Construct the c-shell setenv command.
	 * 
	 * @param name
	 *            of variable
	 * @param value
	 *            of variable
	 * @param buffer
	 *            running contents of the script being generated
	 */
	private static void setenv(String name, String value, StringBuffer buffer) {
		buffer.append(JAXBControlConstants.SETENV).append(JAXBControlConstants.SP).append(name).append(JAXBControlConstants.SP)
				.append(JAXBControlConstants.QT).append(value).append(JAXBControlConstants.QT)
				.append(JAXBControlConstants.REMOTE_LINE_SEP);
	}

	private EnvironmentVariableUtils() {
	}
}
