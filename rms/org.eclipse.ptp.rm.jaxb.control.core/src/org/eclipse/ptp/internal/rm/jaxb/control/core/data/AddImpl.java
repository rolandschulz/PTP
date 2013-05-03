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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.internal.rm.jaxb.control.core.exceptions.StreamParserException;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AddType;
import org.eclipse.ptp.rm.jaxb.core.data.EntryType;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class AddImpl extends AbstractAssign {

	private final List<EntryType> entries;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param add
	 *            JAXB data element
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public AddImpl(String uuid, AddType add, IVariableMap rmVarMap) {
		super(rmVarMap);
		this.uuid = uuid;
		field = add.getField();
		entries = add.getEntry();
		forceNew = add.isForceNewObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) throws StreamParserException {
		List<String> list = null;
		if (previous != null && previous instanceof List<?>) {
			list = (List<String>) previous;
		} else {
			list = new ArrayList<String>();
		}

		if (!entries.isEmpty()) {
			for (EntryType e : entries) {
				Object v = getValue(e, values);
				if (v != null) {
					list.add(v.toString());
				}
			}
		}
		return new Object[] { list };
	}
}
