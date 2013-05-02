/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.launch.variables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.internal.etfw.launch.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * Resolver for the ETFWVariableMap (tag: ${etfw:). <br>
 * <br>
 * In order to guarantee consistency, any implicit call to resolve should be synchronized statically and should be preceded by a
 * call to {@link #setActive(ETFWVariableMap)}. See further {@link #resolveValue(IDynamicVariable, String)}
 * 
 * The ${etfw: tag can contain a name with a suffix, "#field", denoting the field of the retrieved object to access. Hence,
 * ${etfw:arch} would yield a string value for the Attribute object associated with the name 'arch', but
 * ${etfw:arch#description} would return the description string for that attribute. In most cases, ${etfw:arch#value} will be
 * the form the argument takes.
 * 
 * This class is based on @see RMVariableResolver/LCVariableResolver and the work done by Albert Rossi and Jeff Overbey
 * 
 * @author Chris Navarro
 */
public class ETFWVariableResolver implements IDynamicVariableResolver {

	private static ETFWVariableMap fActive;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable,
	 * java.lang.String)
	 */
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (fActive != null && argument != null) {
			String[] parts = argument.split(JAXBControlConstants.PDRX);
			Object value = fActive.get(parts[0]);
			if (value != null) {
				if (parts.length == 2) {
					try {
						return invokeGetter(value, parts[1]);
					} catch (Throwable t) {
						throw CoreExceptionUtils.newException(Messages.ETFWVariableResolver_DE_REF_ERROR, t);
					}
				} else {
					if (value instanceof String && EnvManagerConfigString.isEnvMgmtConfigString((String) value)) {
						if (fActive.getEnvManager() != null) {
							return fActive.getEnvManager().getBashConcatenation(
									"\n", false, new EnvManagerConfigString((String) value), //$NON-NLS-1$
									null);
						} else {
							return JAXBCoreConstants.ZEROSTR;
						}
					} else {
						return String.valueOf(value);
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param active
	 *            current instance of the map
	 */
	public static void setActive(ETFWVariableMap active) {
		fActive = active;
	}

	/**
	 * Auxiliary reflection method for retrieving the field value of the object corresponding to the resolved name.
	 * 
	 * @param target
	 *            Property or Attribute corresponding to the resolved name
	 * @param string
	 *            name of the field
	 * @return string value of the value returned by invoking "get[field]()" on the target
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static String invokeGetter(Object target, String string) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String name = JAXBControlConstants.GET + string.substring(0, 1).toUpperCase() + string.substring(1);
		Method getter = target.getClass().getDeclaredMethod(name, (Class[]) null);
		Object result = getter.invoke(target, (Object[]) null);
		if (result == null) {
			return null;
		}
		return String.valueOf(result);
	}
}
