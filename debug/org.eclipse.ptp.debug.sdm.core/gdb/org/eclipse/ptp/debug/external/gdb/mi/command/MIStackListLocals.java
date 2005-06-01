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

package org.eclipse.ptp.debug.external.gdb.mi.command;




import org.eclipse.ptp.debug.external.gdb.mi.MIException;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIInfo;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIOutput;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIStackListLocalsInfo;




/**
 * 
 *     -stack-list-locals PRINT-VALUES
 *
 *  Display the local variable names for the current frame.  With an
 * argument of 0 prints only the names of the variables, with argument of 1
 * prints also their values.
 * 
 */
public class MIStackListLocals extends MICommand 
{
	public MIStackListLocals(boolean printValues) {
		super("-stack-list-locals"); //$NON-NLS-1$
		if (printValues) {
			setParameters(new String[]{"1"}); //$NON-NLS-1$
		} else {
			setParameters(new String[]{"0"}); //$NON-NLS-1$
		}
	}

	public MIStackListLocalsInfo getMIStackListLocalsInfo() throws MIException {
		return (MIStackListLocalsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIStackListLocalsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
