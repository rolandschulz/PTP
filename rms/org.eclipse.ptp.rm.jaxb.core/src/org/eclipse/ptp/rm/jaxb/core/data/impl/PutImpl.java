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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

public class PutImpl extends AbstractRangeAssign {

	private final Range keys;

	public PutImpl(Put put) {
		this.field = put.getField();
		String rString = put.getKeyGroups();
		if (rString == null) {
			rString = put.getKeyIndices();
		}
		keys = new Range(rString);
		rString = put.getValueGroups();
		if (rString == null) {
			rString = put.getValueIndices();
		}
		range = new Range(rString);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) {
		if (values == null) {
			return new Object[] { previous };
		}
		keys.setLen(values.length);

		List<Object> foundKeys = keys.findInRange(values);
		if (foundKeys.isEmpty()) {
			return new Object[] { previous };
		}

		range.setLen(values.length);
		List<Object> foundValues = range.findInRange(values);
		int sz = foundKeys.size();
		if (sz != foundValues.size()) {
			throw new IllegalStateException(Messages.StreamParserInconsistentMapValues + sz + CM + foundValues.size());
		}

		Map<String, String> map = null;
		if (previous != null && previous instanceof Map<?, ?>) {
			map = (Map<String, String>) previous;
		} else {
			map = new TreeMap<String, String>();
		}

		for (int i = 0; i < sz; i++) {
			map.put(foundKeys.get(i).toString(), (String) foundValues.get(i));
		}

		return new Object[] { map };
	}
}
