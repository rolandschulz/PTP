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

public class LTVariableResolver implements IDynamicVariableResolver, IJAXBNonNLSConstants {

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		LTVariableMap m = LTVariableMap.getActiveInstance();
		if (m != null) {
			String[] split = argument.split(PDRX);
			if (split.length > 1) {
				argument = split[0];
			}
			return m.getVariables().get(argument);
		}
		return null;
	}
}
