package org.eclipse.ptp.rm.jaxb.core.data.impl;

import org.eclipse.ptp.rm.jaxb.core.data.Set;

public class SetImpl extends AbstractAssign {

	private int index;

	private final String value;

	public SetImpl(Set set) {
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
