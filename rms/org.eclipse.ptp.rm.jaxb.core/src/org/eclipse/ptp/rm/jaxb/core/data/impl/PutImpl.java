package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ptp.rm.jaxb.core.data.Put;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

public class PutImpl extends AbstractRangeAssign {

	private final Range keys;

	public PutImpl(String field, Put put) {
		this.field = field;
		keys = new Range(put.getKeyIndices());
		range = new Range(put.getValueIndices());
		this.clzz = new Class[] { Object.class, Object.class };
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object previous, String[] values) {
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
