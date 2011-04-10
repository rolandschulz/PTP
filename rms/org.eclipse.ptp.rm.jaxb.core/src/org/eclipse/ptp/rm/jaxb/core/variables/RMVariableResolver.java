/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.variables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;

/**
 * Resolver for the RMVariableMap (tag: ${rm:). See further
 * {@link #RMVariableResolver()}
 * 
 * @author arossi
 * 
 */
public class RMVariableResolver implements IDynamicVariableResolver, IJAXBNonNLSConstants {

	/**
	 * The ${rm: tag can contain a name with a suffix, "#field", denoting the
	 * field of the retrieved object to access. Hence, ${rm:arch} would yield a
	 * string value for the Attribute object associated with the name 'arch',
	 * but ${rm:arch#description} would return the description string for that
	 * attribute. In most cases, ${rm:arch#value} will be the form the argument
	 * takes.
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		String[] parts = argument.split(PDRX);
		Object value = RMVariableMap.getActiveInstance().get(parts[0]);
		if (value != null) {
			if (parts.length == 2) {
				try {
					return invokeGetter(value, parts[1]);
				} catch (Throwable t) {
					throw CoreExceptionUtils.newException(Messages.RMVariableResolver_derefError, t);
				}
			} else {
				return String.valueOf(value);
			}
		}
		return null;
	}

	/**
	 * Auxiliary reflection method for retrieving the field value of the object
	 * corresponding to the resolved name.
	 * 
	 * @param target
	 *            Property or Attribute corresponding to the resolved name
	 * @param string
	 *            name of the field
	 * @return string value of the value returned by invoking "get[field]()" on
	 *         the target
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static String invokeGetter(Object target, String string) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String name = GET + string.substring(0, 1).toUpperCase() + string.substring(1);
		Method getter = target.getClass().getDeclaredMethod(name, (Class[]) null);
		Object result = getter.invoke(target, (Object[]) null);
		if (result == null) {
			return null;
		}
		return String.valueOf(result);
	}
}
