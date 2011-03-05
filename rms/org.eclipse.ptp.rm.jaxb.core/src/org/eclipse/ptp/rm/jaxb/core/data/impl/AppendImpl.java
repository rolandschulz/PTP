package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Append;

public class AppendImpl extends AbstractRangeAssign {

	private final String separator;
	private final String endSeparator;

	public AppendImpl(String field, Append append) {
		this.field = field;
		separator = append.getSeparator();
		endSeparator = append.getEndSeparator();
		range = new Range(append.getValueIndices());
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
			if (null != separator) {
				buffer.append(separator);
			}
		}
		buffer.append(found.get(0).toString());
		int sz = found.size();
		for (int i = 1; i < sz; i++) {
			if (separator != null) {
				buffer.append(separator);
			}
			buffer.append(found.get(i));
		}
		if (null != endSeparator) {
			buffer.append(endSeparator);
		}
		return new Object[] { buffer.toString() };
	}
}
