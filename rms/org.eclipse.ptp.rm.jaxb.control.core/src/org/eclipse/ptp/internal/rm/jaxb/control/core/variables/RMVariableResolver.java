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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;

/**
 * Resolver for the RMVariableMap (tag: ${ptp_rm:). <br>
 * <br>
 * In order to guarantee consistency, any implicit call to resolve should be synchronized statically and should be preceded by a
 * call to {@link #setActive(RMVariableMap)}. See further {@link #resolveValue(IDynamicVariable, String)}
 * 
 * The ${ptp_rm: tag can contain a name with a suffix, "#field", denoting the field of the retrieved object to access. Hence,
 * ${ptp_rm:arch} would yield a string value for the Attribute object associated with the name 'arch', but
 * ${ptp_rm:arch#description} would return the description string for that attribute. In most cases, ${ptp_rm:arch#value} will be
 * the form the argument takes.
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class RMVariableResolver implements IDynamicVariableResolver {

	private static RMVariableMap fActive;

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

	/**
	 * @param active
	 *            current instance of the map
	 */
	public static void setActive(RMVariableMap active) {
		fActive = active;
	}

	private String resolveEMS(String value) {
		if (!value.equals("") && EnvManagerConfigString.isEnvMgmtConfigString(value)) { //$NON-NLS-1$
			if (fActive.getEnvManager() != null) {
				return fActive.getEnvManager().getBashConcatenation("\n", false, new EnvManagerConfigString(value), //$NON-NLS-1$
						null);
			}
			return ""; //$NON-NLS-1$
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable,
	 * java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (fActive != null && argument != null) {
			String[] parts = argument.split(JAXBControlConstants.PDRX);
			Object value = fActive.get(parts[0]);
			if (value != null) {
				if (parts.length == 2) {
					String result;
					try {
						result = invokeGetter(value, parts[1]);
					} catch (Throwable t) {
						throw CoreExceptionUtils.newException(Messages.RMVariableResolver_derefError, t);
					}
					if (result != null) {
						return resolveEMS(result);
					}
					return result;
				}
				return resolveEMS(String.valueOf(value));
			}
		}
		return null;
	}
}
