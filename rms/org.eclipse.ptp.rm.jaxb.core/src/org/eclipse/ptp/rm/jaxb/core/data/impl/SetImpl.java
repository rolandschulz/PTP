package org.eclipse.ptp.rm.jaxb.core.data.impl;

import org.eclipse.ptp.rm.jaxb.core.data.Set;

public class SetImpl extends AbstractAssign {

	private final int index;

	public SetImpl(String field, Set set) {
		this.field = field;
		index = set.getValueIndex();
		this.clzz = new Class[] { Object.class };
	}

	@Override
	protected Object[] getValue(Object previous, String[] values) {
		return new Object[] { values[index] };
	}
}
