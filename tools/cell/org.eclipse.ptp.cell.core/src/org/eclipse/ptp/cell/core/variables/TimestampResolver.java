/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.core.variables;

import java.util.Calendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.cell.debug.Debug;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class TimestampResolver implements IDynamicVariableResolver {

	public TimestampResolver() {
	}

	/**
	 * Resolves and returns a value for the timestamp variable. The argument value is discarded.
	 *  
	 * @param variable variable to resolve a value for; in this case it is always timestamp
	 * @param argument argument present in expression or <code>null</code> if none
	 * @return variable value, possibly <code>null</code>
	 * @throws CoreException if unable to resolve a value for the given variable
	 */
	public String resolveValue(IDynamicVariable variable, String argument) {
		String result = Long.toString(Calendar.getInstance().getTimeInMillis());
		Debug.POLICY.pass(Debug.DEBUG_VARIABLES, variable, argument, result);
		return result;
	}

}
