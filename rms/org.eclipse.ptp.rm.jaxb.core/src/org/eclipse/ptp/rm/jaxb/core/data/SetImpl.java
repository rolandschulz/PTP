package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableResolver;

public class SetImpl extends AbstractRangePart implements IJAXBNonNLSConstants {

	private final Object target;
	private final String setter;

	public SetImpl(Set set) throws Throwable {
		RMVariableMap map = RMVariableMap.getActiveInstance();
		assert (null != map);
		Map<String, Object> vars = map.getVariables();
		String name = set.getName();
		setter = set.getSetter();
		target = vars.get(name);
		if (target == null) {
			if (!set.isDiscovered()) {
				throw new Exception(Messages.StreamParserNoSuchVariableError + name);
			} else {
				RMVariableMap.getActiveInstance().getDiscovered().put(name, target);
			}
		}
		String exp = set.getRange();
		if (exp != null) {
			range = new Range(exp);
		}
	}

	public void doSet(int i, String segment) throws Throwable {
		if (range != null && !range.isInRange(i)) {
			return;
		}
		String value = RMVariableResolver.invokeGetter(target, setter);
		if (value != null) {
			segment = value + segment;
		}
		RMVariableResolver.invokeSetter(target, setter, segment);
	}
}