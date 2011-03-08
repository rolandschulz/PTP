/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

public abstract class AbstractAssign implements IAssign, IJAXBNonNLSConstants {

	protected String uuid;
	protected String field;
	protected Object target;

	public void assign(String[] values) throws Throwable {
		Object previous = get(target, field);
		set(target, field, getValue(previous, values));
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	protected abstract Object[] getValue(Object previous, String[] values);

	static Object get(Object target, String field) throws Throwable {
		String name = GET + field.substring(0, 1).toUpperCase() + field.substring(1);
		Method method = null;
		try {
			method = target.getClass().getMethod(name, (Class[]) null);
		} catch (Throwable t) {
			name = IS + field.substring(0, 1).toUpperCase() + field.substring(1);
			method = target.getClass().getMethod(name, (Class[]) null);
		}
		return method.invoke(target, (Object[]) null);
	}

	static void set(Object target, String field, Object[] values) throws Throwable {
		String name = SET + field.substring(0, 1).toUpperCase() + field.substring(1);
		Method[] methods = target.getClass().getMethods();
		Method setter = null;
		for (Method m : methods) {
			if (m.getName().equals(name)) {
				setter = m;
			}
		}
		if (setter == null) {
			throw new NoSuchMethodException(name + CO + SP + target);
		}
		if (values[0] != null) {
			Class<?>[] mclzz = setter.getParameterTypes();
			// better have 1 parameter
			Class<?> param = mclzz[0];
			Class<?> valueClass = values[0].getClass();
			Throwable t = new IllegalArgumentException(name + SP + valueClass);
			if (!param.equals(Object.class) && !param.isAssignableFrom(values[0].getClass())) {
				if (valueClass.equals(String.class)) {
					/*
					 * TODO: evaluate expression
					 */
					if (param.equals(Boolean.class)) {
						values[0] = new Boolean(values[0].toString());
					} else if (param.equals(Integer.class)) {
						values[0] = new Integer(values[0].toString());
					} else if (param.equals(BigInteger.class)) {
						values[0] = new BigInteger(values[0].toString());
					} else {
						throw t;
					}
				} else if (valueClass.equals(Integer.class) || valueClass.equals(BigInteger.class)
						|| valueClass.equals(Boolean.class)) {
					if (param.equals(String.class)) {
						values[0] = values[0].toString();
					} else {
						throw t;
					}
				} else {
					throw t;
				}
			}
		}
		setter.invoke(target, values);
	}
}
