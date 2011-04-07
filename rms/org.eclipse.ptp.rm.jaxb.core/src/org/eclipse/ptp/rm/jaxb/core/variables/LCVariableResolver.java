package org.eclipse.ptp.rm.jaxb.core.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

public class LCVariableResolver implements IDynamicVariableResolver, IJAXBNonNLSConstants {

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		LCVariableMap m = LCVariableMap.getActiveInstance();
		if (m != null) {
			String[] split = argument.split(PDRX);
			if (split.length > 1) {
				if (split[1].equals(VALUE)) {
					argument = split[0];
				}
			}
			return (String) m.get(argument);
		}
		return null;
	}
}