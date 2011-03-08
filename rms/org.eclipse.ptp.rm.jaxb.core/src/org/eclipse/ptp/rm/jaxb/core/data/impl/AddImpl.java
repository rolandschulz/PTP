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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Add;

public class AddImpl extends AbstractRangeAssign {

	private final List<String> values;

	public AddImpl(String uuid, Add add) {
		this.uuid = uuid;
		this.field = add.getField();
		String rString = add.getGroups();
		if (rString == null) {
			rString = add.getIndices();
		}
		range = new Range(rString);
		values = add.getValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) throws Throwable {
		if (!this.values.isEmpty()) {
			List<String> norm = new ArrayList<String>();
			for (String v : this.values) {
				norm.add((String) normalizedValue(target, uuid, v));
			}
			return new Object[] { norm };
		}

		if (values == null) {
			return new Object[] { previous };
		}
		range.setLen(values.length);
		List<Object> found = range.findInRange(values);
		if (found.isEmpty()) {
			return new Object[] { previous };
		}

		List<String> list = null;
		if (previous != null && previous instanceof List<?>) {
			list = (List<String>) previous;
		} else {
			list = new ArrayList<String>();
		}

		for (Object o : found) {
			list.add(o.toString());
		}

		return new Object[] { list };
	}
}
