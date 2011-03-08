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

import org.eclipse.ptp.rm.jaxb.core.data.Set;

public class SetImpl extends AbstractAssign {

	private int index;

	private final String value;

	public SetImpl(String uuid, Set set) {
		this.uuid = uuid;
		this.field = set.getField();
		index = set.getIndex();
		int group = set.getGroup();
		if (index == 0 && group != 0) {
			index = group;
		}
		value = set.getValue();
	}

	@Override
	protected Object[] getValue(Object previous, String[] values) {
		if (value != null) {
			return new Object[] { value };
		}
		if (values == null) {
			return new Object[] { previous };
		}
		return new Object[] { values[index] };
	}
}
