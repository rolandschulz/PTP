package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Append;

public class AppendImpl extends AbstractRangeAssign {

	private final String delim;
	private final boolean delimAtEnd;

	public AppendImpl(String field, Append append) {
		this.field = field;
		delim = append.getDelim();
		delimAtEnd = append.isDelimAtEnd();
		range = new Range(append.getValues());
		this.clzz = new Class[] { Object.class };
	}

	@Override
	protected Object[] getValue(Object previous, String[] values) {
		range.setLen(values.length);
		List<Object> found = range.findInRange(values);
		if (found.isEmpty()) {
			return new Object[] { previous };
		}
		StringBuffer buffer = new StringBuffer();
		if (previous != null && previous instanceof String) {
			buffer.append(previous);
			if (!delimAtEnd) {
				buffer.append(delim);
			}
		}
		buffer.append(found.get(0).toString());
		int sz = found.size();
		for (int i = 1; i < sz; i++) {
			if (delim != null) {
				buffer.append(delim);
			}
			buffer.append(found.get(i));
		}
		if (delimAtEnd) {
			buffer.append(delim);
		}
		return new Object[] { buffer.toString() };
	}
}
