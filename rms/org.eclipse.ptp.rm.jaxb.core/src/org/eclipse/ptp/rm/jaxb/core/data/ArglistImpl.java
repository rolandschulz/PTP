package org.eclipse.ptp.rm.jaxb.core.data;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ArglistImpl implements IJAXBNonNLSConstants {

	private final Arglist args;
	private final RMVariableMap map;

	public ArglistImpl(Arglist args) {
		this.args = args;
		this.map = RMVariableMap.getInstance();
	}

	public void toString(StringBuffer buffer) {
		if (args.dynamicAppend) {
			composeDynamicArgs(buffer);
		} else {
			composeStandardArgs(buffer);
		}
	}

	/*
	 * Iterate over all dynamic attributes, appending the sequence of args for
	 * each attribute. By convention, '${@name}' and '${@value}' will refer to a
	 * given dynamic attribute name and value (of undefined position i in the
	 * list).
	 */
	private void composeDynamicArgs(StringBuffer buffer) {
		for (String name : map.getDiscovered().keySet()) {
			for (Arg arg : args.getArg()) {
				String content = arg.getContent();
				content = content.replaceAll(AMP, name + PD);
				content = map.getString(content);
				String undefined = arg.getIsUndefinedIfEquals();
				if (undefined != null && undefined.equals(content)) {
					continue;
				}
				if (buffer.length() > 0) {
					buffer.append(SP);
				}
				buffer.append(content);
			}
		}
	}

	private void composeStandardArgs(StringBuffer buffer) {
		for (Arg arg : args.getArg()) {
			String dereferenced = map.getString(arg.getContent());
			String undefined = arg.getIsUndefinedIfEquals();
			if (undefined != null && undefined.equals(dereferenced)) {
				continue;
			}
			if (buffer.length() > 0) {
				buffer.append(SP);
			}
			buffer.append(dereferenced);
		}
	}
}
