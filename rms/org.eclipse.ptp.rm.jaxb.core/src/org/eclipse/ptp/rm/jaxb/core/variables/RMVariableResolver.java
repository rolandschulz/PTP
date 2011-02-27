package org.eclipse.ptp.rm.jaxb.core.variables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;

public class RMVariableResolver implements IDynamicVariableResolver, IJAXBNonNLSConstants {

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		Map<String, Object> variables = RMVariableMap.getActiveInstance().getVariables();
		String[] parts = argument.split(PDRX);
		Object value = variables.get(parts[0]);
		if (value != null) {
			if (parts.length == 2) {
				try {
					return invokeGetter(value, parts[1]);
				} catch (Throwable t) {
					throw CoreExceptionUtils.newException(Messages.RMVariableResolver_derefError, t);
				}
			} else {
				return value.toString();
			}
		}
		return null;
	}

	public static String invokeGetter(Object target, String string) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String name = GET + string.substring(0, 1).toUpperCase() + string.substring(1);
		Method getter = target.getClass().getDeclaredMethod(name, (Class[]) null);
		Object result = getter.invoke(target, (Object[]) null);
		if (result == null) {
			return null;
		}
		return result.toString();
	}

	public static void invokeSetter(Object target, String string, Object value) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String name = SET + string.substring(0, 1).toUpperCase() + string.substring(1);
		Method setter = value.getClass().getDeclaredMethod(name, new Class[] { value.getClass() });
		setter.invoke(value, new Object[] { value });
	}
}
