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

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AppendType;
import org.eclipse.ptp.rm.jaxb.core.data.EntryType;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class AppendImpl extends AbstractAssign {

	private final String separator;
	private final List<EntryType> entries;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param append
	 *            JAXB data element
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public AppendImpl(String uuid, AppendType append, IVariableMap rmVarMap) {
		super(rmVarMap);
		this.uuid = uuid;
		this.field = append.getField();
		separator = append.getSeparator();
		entries = append.getEntry();
		forceNew = append.isForceNewObject();
	}

	@Override
	protected Object[] getValue(Object previous, String[] values) throws Throwable {
		StringBuffer buffer = new StringBuffer();
		if (!entries.isEmpty()) {
			if (previous != null) {
				buffer.append(previous);
				if (separator != null) {
					buffer.append(separator);
				}
			}
			Object v = getValue(entries.get(0), values);
			if (v != null) {
				buffer.append(v);
			}
			int len = entries.size();
			for (int i = 1; i < len; i++) {
				v = getValue(entries.get(i), values);
				if (v != null) {
					if (separator != null) {
						buffer.append(separator);
					}
					buffer.append(v);
				}
			}
		} else {
			buffer.append(previous);
		}
		return new Object[] { buffer.toString() };
	}
}
