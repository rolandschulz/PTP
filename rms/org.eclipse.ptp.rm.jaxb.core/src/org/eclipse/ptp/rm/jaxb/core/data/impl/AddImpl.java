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

	public AddImpl(Add add) {
		this.field = add.getField();
		String rString = add.getGroups();
		if (rString == null) {
			rString = add.getIndices();
		}
		range = new Range(rString);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) {
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
