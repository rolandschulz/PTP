package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Add;

public class AddImpl extends AbstractRangeAssign {

	public AddImpl(String field, Add add) {
		this.field = field;
		range = new Range(add.getValues());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) {
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
