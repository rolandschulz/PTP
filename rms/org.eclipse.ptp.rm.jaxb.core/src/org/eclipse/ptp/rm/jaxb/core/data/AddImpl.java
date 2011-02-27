package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class AddImpl extends AbstractRangePart {

	private List<String> list;

	@SuppressWarnings("unchecked")
	public AddImpl(Add add) throws CoreException {
		RMVariableMap map = RMVariableMap.getActiveInstance();
		assert (null != map);
		Map<String, Object> vars = RMVariableMap.getActiveInstance().getVariables();
		String name = add.getName();
		list = (List<String>) vars.get(name);
		if (list == null) {
			list = new ArrayList<String>();
			vars.put(name, list);
		}
		String exp = add.getRange();
		if (exp != null) {
			range = new Range(exp);
		}
	}

	public void doAdd(int i, String segment) {
		if (range == null || range.isInRange(i)) {
			list.add(segment);
		}
	}
}
