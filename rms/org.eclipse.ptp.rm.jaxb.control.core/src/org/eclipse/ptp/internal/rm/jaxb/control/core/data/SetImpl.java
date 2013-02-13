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

import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.EntryType;
import org.eclipse.ptp.rm.jaxb.core.data.SetType;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class SetImpl extends AbstractAssign {

	private final EntryType entry;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param set
	 *            JAXB data element
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public SetImpl(String uuid, SetType set, IVariableMap rmVarMap) {
		super(rmVarMap);
		this.uuid = uuid;
		field = set.getField();
		entry = set.getEntry();
		forceNew = set.isForceNewObject();
	}

	@Override
	protected Object[] getValue(Object previous, String[] values) throws Throwable {
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
