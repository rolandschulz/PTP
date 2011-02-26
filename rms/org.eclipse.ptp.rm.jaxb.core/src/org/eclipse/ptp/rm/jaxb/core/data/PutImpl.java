package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class PutImpl extends AbstractRangePart {

	private final Property property;

	public PutImpl(Put put) {
		Map<String, Object> vars = RMVariableMap.getInstance().getVariables();
		String name = put.getName();
		property = new Property();
		property.setName(name);
		vars.put(name, property);
		String exp = put.getRange();
		if (exp != null) {
			range = new Range(exp);
		}
	}

	public void doPut(int i, String segment) {
		if (range == null || range.isInRange(i)) {
			Object value = property.getValue();
			if (value != null && value instanceof String) {
				segment = value.toString() + segment;
			}
			property.setValue(segment);
		}
	}
}
