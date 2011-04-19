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

import org.eclipse.ptp.rm.jaxb.core.data.Entry;
import org.eclipse.ptp.rm.jaxb.core.data.Set;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class SetImpl extends AbstractAssign {

	private final Entry entry;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param set
	 *            JAXB data element
	 */
	public SetImpl(String uuid, Set set) {
		this.uuid = uuid;
		field = set.getField();
		entry = set.getEntry();
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