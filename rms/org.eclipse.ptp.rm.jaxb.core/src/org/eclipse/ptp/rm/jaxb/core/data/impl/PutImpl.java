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

import org.eclipse.ptp.rm.jaxb.core.data.Entry;
import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class PutImpl extends AbstractAssign {

	private final List<Entry> entries;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param put
	 *            JAXB data element
	 */
	public PutImpl(String uuid, Put put) {
		this.uuid = uuid;
		field = put.getField();
		entries = put.getEntry();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) throws Throwable {
		Map<String, String> map = null;
		if (previous != null && previous instanceof Map<?, ?>) {
			map = (Map<String, String>) previous;
		} else {
			map = new TreeMap<String, String>();
		}

		if (!entries.isEmpty()) {
			for (Entry e : entries) {
				String k = getKey(e, values);
				if (k == null) {
					throw new IllegalStateException(Messages.StreamParserInconsistentMapValues + e.getKey() + CM + e.getKeyGroup()
							+ CM + e.getKeyIndex());
				}
				String v = (String) getValue(e, values);
				map.put(k, v);
			}
		}
		return new Object[] { map };
	}
}
