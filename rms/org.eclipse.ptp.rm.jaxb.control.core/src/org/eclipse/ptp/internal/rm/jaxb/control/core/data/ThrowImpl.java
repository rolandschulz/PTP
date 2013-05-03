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

import org.eclipse.ptp.internal.rm.jaxb.control.core.exceptions.StreamParserException;
import org.eclipse.ptp.internal.rm.jaxb.control.core.exceptions.UserThrownException;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.EntryType;
import org.eclipse.ptp.rm.jaxb.core.data.ThrowType;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class ThrowImpl extends AbstractAssign {

	private final EntryType entry;
	private final String message;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param throwType
	 *            JAXB data element
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public ThrowImpl(String uuid, ThrowType throwType, IVariableMap rmVarMap) {
		super(rmVarMap);
		this.uuid = uuid;
		this.message = throwType.getMessage();
		field = throwType.getField();
		entry = throwType.getEntry();
	}

	/**
	 * Overridden to throw the exception.
	 */
	@Override
	public void assign(String[] values) throws StreamParserException {
		Object previous = get(target, field);
		if (field != null) {
			set(target, field, getValue(previous, values));
			index++;
		}
		throw new UserThrownException(rmVarMap.getString(uuid, message));
	}

	@Override
	protected Object[] getValue(Object previous, String[] values) throws StreamParserException {
		if (entry == null) {
			return null;
		}

		Object value = getValue(entry, values);
		if (value != null) {
			return new Object[] { value };
		}

		return new Object[] { previous };
	}
}
