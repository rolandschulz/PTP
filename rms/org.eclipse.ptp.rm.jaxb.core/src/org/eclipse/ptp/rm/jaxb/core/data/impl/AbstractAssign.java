package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.lang.reflect.Method;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

public abstract class AbstractAssign implements IAssign, IJAXBNonNLSConstants {

	protected String field;

	public void assign(Object target, String[] values) throws Throwable {
		Object previous = get(target, field);
		set(target, field, getValue(previous, values));
	}

	protected abstract Object[] getValue(Object previous, String[] values);

	private Object get(Object target, String field) throws Throwable {
		String name = GET + field.substring(0, 1).toUpperCase() + field.substring(1);
		Method method = target.getClass().getMethod(name, (Class[]) null);
		return method.invoke(target, (Object[]) null);
	}

	private void set(Object target, String field, Object[] values) throws Throwable {
		@SuppressWarnings("rawtypes")
		Class[] clzz = new Class[values.length];
		for (int i = 0; i < clzz.length; i++) {
			clzz[i] = values[i].getClass();
		}
		String name = SET + field.substring(0, 1).toUpperCase() + field.substring(1);
		Method method = target.getClass().getMethod(name, clzz);
		method.invoke(target, values);
	}
}
