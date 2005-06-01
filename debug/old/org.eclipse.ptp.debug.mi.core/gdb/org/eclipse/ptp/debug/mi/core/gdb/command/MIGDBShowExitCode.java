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
import org.eclipse.ptp.debug.mi.core.gdb.output.MIGDBShowExitCodeInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIOutput;



/**
 * 
 *-data-evaluate-expression $_exitcode
 * ^done,value="10"
 *
 *   Show the current value of a $_exitcode
 * 
 */
public class MIGDBShowExitCode extends MIDataEvaluateExpression {

	public MIGDBShowExitCode() {
		super("$_exitcode"); //$NON-NLS-1$
	}

	public MIGDBShowExitCodeInfo getMIGDBShowExitCodeInfo() throws MIException {
		return (MIGDBShowExitCodeInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowExitCodeInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
