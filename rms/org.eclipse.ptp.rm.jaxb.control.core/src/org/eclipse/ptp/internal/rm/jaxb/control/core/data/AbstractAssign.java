/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.internal.rm.jaxb.control.core.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rm.jaxb.control.core.IAssign;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.utils.DebuggingLogger;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.control.core.exceptions.StreamParserException;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AddType;
import org.eclipse.ptp.rm.jaxb.core.data.AppendType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.EntryType;
import org.eclipse.ptp.rm.jaxb.core.data.PutType;
import org.eclipse.ptp.rm.jaxb.core.data.SetType;
import org.eclipse.ptp.rm.jaxb.core.data.ThrowType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Base class for the wrappers around the data objects providing information as to the operations on a target property to be
 * undertaken when there is a match.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractAssign implements IAssign {

	/**
	 * Auxiliary for adding a wrapper implementation.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param assign
	 *            the JAXB element class.
	 * @param list
	 *            the list of wrappers
	 * @param rmVarMap
	 *            resource manager environment
	 */
	static void add(String uuid, Object assign, List<IAssign> list, IVariableMap rmVarMap) {
		if (assign instanceof AddType) {
			AddType add = (AddType) assign;
			list.add(new AddImpl(uuid, add, rmVarMap));
			return;
		}
		if (assign instanceof AppendType) {
			AppendType append = (AppendType) assign;
			list.add(new AppendImpl(uuid, append, rmVarMap));
			return;
		}
		if (assign instanceof PutType) {
			PutType put = (PutType) assign;
			list.add(new PutImpl(uuid, put, rmVarMap));
			return;
		}
		if (assign instanceof SetType) {
			SetType set = (SetType) assign;
			list.add(new SetImpl(uuid, set, rmVarMap));
			return;
		}
		if (assign instanceof ThrowType) {
			ThrowType thr = (ThrowType) assign;
			list.add(new ThrowImpl(uuid, thr, rmVarMap));
			return;
		}
	}

	/**
	 * Auxiliary using Java reflection to perform a get on the target.
	 * 
	 * @param target
	 *            AttributeType
	 * @param field
	 *            on the target from which to retrieve the value.
	 * @return value of the field
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	static Object get(AttributeType target, String field) throws StreamParserException {
		if (field == null) {
			return null;
		}
		String name = JAXBControlConstants.GET + field.substring(0, 1).toUpperCase() + field.substring(1);
		Method method = null;
		try {
			method = target.getClass().getMethod(name, (Class[]) null);
		} catch (Exception e) {
			name = JAXBControlConstants.IS + field.substring(0, 1).toUpperCase() + field.substring(1);
		}
		try {
			if (method == null) {
				method = target.getClass().getMethod(name, (Class[]) null);
			}
			return method.invoke(target, (Object[]) null);
		} catch (Exception e) {
			throw new StreamParserException(NLS.bind(Messages.AbstractAssign_Unable_to_get_attribute_value, e.getMessage()), e);
		}
	}

	/**
	 * Determines whether the input represents an object field or a resolvable expression. Also converts string to int or boolean,
	 * if applicable,
	 * 
	 * @param target
	 *            on which to apply the getter.
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param expression
	 *            input to be interpreted
	 * @param convert
	 *            expression to boolean or int, if applicable.
	 * @param rmVarMap
	 *            resource manager environment
	 * @return value after dereferencing or normalization
	 * @throws CoreException
	 */
	static Object normalizedValue(AttributeType target, String uuid, String expression, boolean convert, IVariableMap map)
			throws StreamParserException {
		Object value = expression;
		if (expression.startsWith(JAXBControlConstants.PD)) {
			if (target == null) {
				return null;
			}
			String field = expression.substring(1);
			value = AbstractAssign.get(target, field);
		} else if (expression.indexOf(JAXBControlConstants.OPENV) >= 0) {
			expression = map.getString(uuid, expression);
			value = map.getString(uuid, expression);
		}
		if (convert) {
			return convert(value);
		}
		return value;
	}

	/**
	 * Auxiliary using Java reflection to perform a set on the target.
	 * 
	 * @param target
	 *            Property or Attribute
	 * @param field
	 *            on the target on which to set the value.
	 * @param values
	 *            corresonding to the parameter(s) of the set method (usually a single one)
	 * @throws StreamParserException
	 */
	static void set(Object target, String field, Object[] values) throws StreamParserException {
		String name = JAXBControlConstants.SET + field.substring(0, 1).toUpperCase() + field.substring(1);
		Method[] methods = target.getClass().getMethods();
		Method setter = null;
		for (Method m : methods) {
			if (m.getName().equals(name)) {
				setter = m;
			}
		}
		if (setter == null) {
			throw new StreamParserException(NLS.bind(Messages.AbstractAssign_No_such_method, name));
		}
		if (values != null && values[0] != null) {
			Class<?>[] mclzz = setter.getParameterTypes();
			// better have 1 parameter
			Class<?> param = mclzz[0];
			Class<?> valueClass = values[0].getClass();
			StreamParserException e = new StreamParserException(Messages.AbstractAssign_Invalid_arguments);
			if (!param.equals(Object.class) && !param.isAssignableFrom(values[0].getClass())) {
				if (valueClass.equals(String.class)) {
					if (param.equals(Boolean.class)) {
						values[0] = new Boolean(values[0].toString());
					} else if (param.equals(Integer.class)) {
						values[0] = new Integer(values[0].toString());
					} else if (param.equals(BigInteger.class)) {
						values[0] = new BigInteger(values[0].toString());
					} else {
						throw e;
					}
				} else if (valueClass.equals(Integer.class) || valueClass.equals(BigInteger.class)
						|| valueClass.equals(Boolean.class)) {
					if (param.equals(String.class)) {
						values[0] = values[0].toString();
					} else {
						throw e;
					}
				} else {
					throw e;
				}
			}
		}
		try {
			setter.invoke(target, values);
		} catch (Exception e) {
			throw new StreamParserException(NLS.bind(Messages.AbstractAssign_Unable_to_set_attribute_value, e.getMessage()), e);
		}
	}

	/**
	 * Converts string to int or boolean, if applicable,
	 * 
	 * @param value
	 * @return converted value
	 */
	private static Object convert(Object value) {
		if (value instanceof String) {
			String string = (String) value;
			if (JAXBControlConstants.TRUE.equalsIgnoreCase(string)) {
				return true;
			}
			if (JAXBControlConstants.FALSE.equalsIgnoreCase(string)) {
				return false;
			}
			try {
				return new Integer(string);
			} catch (NumberFormatException nfe) {
				// Ignore
			}
		}
		return value;
	}

	protected String uuid;
	protected String field;
	protected AttributeType target;
	protected int index;
	protected boolean forceNew;
	protected IVariableMap rmVarMap;

	/**
	 * @param rmVarMap
	 *            resource manager environment
	 */
	protected AbstractAssign(IVariableMap rmVarMap) {
		uuid = null;
		field = null;
		target = null;
		index = 0;
		forceNew = false;
		this.rmVarMap = rmVarMap;
	}

	/**
	 * Applies the assignment.
	 * 
	 * @param values
	 *            from the expression parsing (groups or segments)
	 * @throws CoreException
	 */
	public void assign(String[] values) throws StreamParserException {
		Object[] value;
		Object previous = get(target, field);
		value = getValue(previous, values);
		set(target, field, value);
		index++;
		DebuggingLogger.getLogger().logActionInfo(
				Messages.AbstractAssign_0 + this + Messages.AbstractAssign_1 + target + Messages.AbstractAssign_2 + field
						+ Messages.AbstractAssign_3 + Arrays.asList(value));
		DebuggingLogger.getLogger().logActionInfo(this + Messages.AbstractAssign_4 + index);
	}

	/**
	 * Used in the case of references to targets constructed in connection with the tokenization. The assumption is that an Assign
	 * action will be applied only once to any given target, in the order of their construction; this index keeps track of where
	 * this particular assign action is in the list.
	 * 
	 * @return the index of the current target
	 */
	public int getIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.IAssign#incrementIndex(int)
	 */
	public void incrementIndex(int increment) {
		index += increment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.IAssign#isForceNew()
	 */
	public boolean isForceNew() {
		return forceNew;
	}

	/**
	 * @param target
	 *            the current property or attribute
	 */
	public void setTarget(AttributeType target) {
		this.target = target;
	}

	/**
	 * Decides whether the index of the map key is a segment (from regex.split()) or a regex group number.
	 * 
	 * @param entry
	 *            from the target map
	 * @return the key index
	 */
	protected int determineKeyIndex(EntryType entry) {
		int index = entry.getKeyIndex();
		int group = entry.getKeyGroup();
		if (index == 0 && group != 0) {
			index = group;
		}
		return index;
	}

	/**
	 * Decides whether the index of the map value is a segment (from regex.split()) or a regex group number.
	 * 
	 * @param entry
	 *            carries the key and value indices
	 * @see org.eclipse.ptp.rm.jaxb.core.data.Entry
	 * @return the value index
	 */
	protected int determineValueIndex(EntryType entry) {
		int index = entry.getValueIndex();
		int group = entry.getValueGroup();
		if (index == 0 && group != 0) {
			index = group;
		}
		return index;
	}

	/**
	 * Find the map key
	 * 
	 * @param entry
	 *            carries the key and value indices
	 * @see org.eclipse.ptp.rm.jaxb.core.data.Entry
	 * @param values
	 *            the parsed result of the expression match
	 * @return key
	 * @throws StreamParserException
	 */
	protected String getKey(final EntryType entry, final String[] values) throws StreamParserException {
		String k = entry.getKey();
		if (k != null) {
			return (String) normalizedValue(target, uuid, k, false, rmVarMap);
		}
		int index = determineKeyIndex(entry);
		if (values != null) {
			if (index >= values.length) {
				new UIJob(Messages.BadEntryIndex) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.BadEntryIndex, entryKeyInfo(entry)
								+ JAXBCoreConstants.CM + JAXBCoreConstants.SP + Arrays.asList(values));
						return Status.OK_STATUS;
					}
				}.schedule();
				/*
				 * go ahead and throw the exception
				 */
			}
			return values[index];
		}
		return null;
	}

	/**
	 * Find the map value
	 * 
	 * @param entry
	 *            carries the key and value indices
	 * @see org.eclipse.ptp.rm.jaxb.core.data.Entry
	 * @param values
	 *            the parsed result of the expression match
	 * @return value
	 * @throws StreamParserException
	 */
	protected Object getValue(final EntryType entry, final String[] values) throws StreamParserException {
		String v = entry.getValue();
		if (v != null) {
			return normalizedValue(target, uuid, v, true, rmVarMap);
		}
		int index = determineValueIndex(entry);
		if (values != null) {
			if (index >= values.length) {
				new UIJob(Messages.BadEntryIndex) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.BadEntryIndex,
								entryValueInfo(entry) + JAXBCoreConstants.CM + JAXBCoreConstants.LINE_SEP + values.length
										+ Messages.AbstractAssign_9 + Arrays.asList(values));
						return Status.OK_STATUS;
					}
				}.schedule();
				/*
				 * go ahead and throw the exception
				 */
			}
			return values[index];
		}
		return null;
	}

	/**
	 * Method specific to the assign type for retrieving the values from the matched expression.
	 * 
	 * @param previous
	 *            the value currently assigned to the field of the target in question.
	 * @param values
	 *            the parsed result of the expression match
	 * @return the value(s) retrieved from the parsed result
	 * @throws StreamParserException
	 */
	protected abstract Object[] getValue(Object previous, String[] values) throws StreamParserException;

	/**
	 * Prints indices for key.
	 * 
	 * @param entry
	 * @return message
	 */
	private String entryKeyInfo(EntryType entry) {
		return JAXBCoreConstants.OPENP + Messages.AbstractAssign_5 + entry.getKeyGroup() + Messages.AbstractAssign_6
				+ entry.getKeyIndex() + JAXBCoreConstants.CLOSP;
	}

	/**
	 * Prints indices for value.
	 * 
	 * @param entry
	 * @return message
	 */
	private String entryValueInfo(EntryType entry) {
		return JAXBCoreConstants.OPENP + Messages.AbstractAssign_7 + entry.getValueGroup() + Messages.AbstractAssign_8
				+ entry.getValueIndex() + JAXBCoreConstants.CLOSP;
	}
}
