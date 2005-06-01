/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.debug.mi.core.gdb.command;



import org.eclipse.ptp.debug.mi.core.gdb.MIException;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIDataEvaluateExpressionInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIOutput;



/**
 * 
 *      -data-evaluate-expression EXPR
 *
 *   Evaluate EXPR as an expression.  The expression could contain an
 *inferior function call.  The function call will execute synchronously.
 *If the expression contains spaces, it must be enclosed in double quotes.
 *
 */
public class MIDataEvaluateExpression extends MICommand 
{
	public MIDataEvaluateExpression(String expr) {
		super("-data-evaluate-expression", new String[]{expr}); //$NON-NLS-1$
	}

	public MIDataEvaluateExpressionInfo getMIDataEvaluateExpressionInfo() throws MIException {
		return (MIDataEvaluateExpressionInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataEvaluateExpressionInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
