/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
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
			Object value = m.get(argument);
			if (value != null) {
				return String.valueOf(value);
			}
		}
		return ZEROSTR;
	}
}