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
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Add;
import org.eclipse.ptp.rm.jaxb.core.data.Append;
import org.eclipse.ptp.rm.jaxb.core.data.Entry;
import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.data.Set;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

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

	protected int determineKeyIndex(Entry entry) {
		int index = entry.getKeyIndex();
		int group = entry.getKeyGroup();
		if (index == 0 && group != 0) {
			index = group;
		}
		return index;
	}

	protected int determineValueIndex(Entry entry) {
		int index = entry.getValueIndex();
		int group = entry.getValueGroup();
		if (index == 0 && group != 0) {
			index = group;
		}
		return index;
	}

	protected String getKey(Entry e, String[] values) throws Throwable {
		String k = e.getKey();
		if (k != null) {
			return (String) normalizedValue(target, uuid, k, false);
		}
		int index = determineKeyIndex(e);
		if (values != null) {
			return values[index];
		}
		return null;
	}

	protected Object getValue(Entry e, String[] values) throws Throwable {
		String v = e.getValue();
		if (v != null) {
			return normalizedValue(target, uuid, v, true);
		}
		int index = determineValueIndex(e);
		if (values != null) {
			return values[index];
		}
		return null;
	}

	protected abstract Object[] getValue(Object previous, String[] values) throws Throwable;

	static void add(String uuid, Object assign, List<IAssign> list) {
		if (assign instanceof Add) {
			Add add = (Add) assign;
			list.add(new AddImpl(uuid, add));
			return;
		}
		if (assign instanceof Append) {
			Append append = (Append) assign;
			list.add(new AppendImpl(uuid, append));
			return;
		}
		if (assign instanceof Put) {
			Put put = (Put) assign;
			list.add(new PutImpl(uuid, put));
			return;
		}
		if (assign instanceof Set) {
			Set set = (Set) assign;
			list.add(new SetImpl(uuid, set));
			return;
		}
	}

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

	static Object normalizedValue(Object target, String uuid, String expression, boolean convert) throws Throwable {
		if (expression.startsWith(THIS)) {
			if (target == null) {
				return null;
			}
			String field = expression.substring(5);
			return AbstractAssign.get(target, field);
		} else if (expression.indexOf(OPENV) >= 0) {
			expression = RMVariableMap.getActiveInstance().getString(uuid, expression);
			return RMVariableMap.getActiveInstance().getString(uuid, expression);
		} else if (convert) {
			if (TRUE.equalsIgnoreCase(expression)) {
				return true;
			}
			if (FALSE.equalsIgnoreCase(expression)) {
				return false;
			}
			try {
				if (expression.indexOf(DOT) >= 0) {
					return new Double(expression);
				}
				return new Integer(expression);
			} catch (NumberFormatException nfe) {
				return expression;
			}
		}
		return expression;
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
