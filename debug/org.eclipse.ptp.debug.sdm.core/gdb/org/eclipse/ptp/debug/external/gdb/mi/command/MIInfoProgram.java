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
import org.eclipse.ptp.debug.external.gdb.mi.output.MIInfoProgramInfo;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIOutput;




/**
 * 
 *    info threads
 *
 */
public class MIInfoProgram extends CLICommand 
{
	public MIInfoProgram() {
		super("info program"); //$NON-NLS-1$
	}

	public MIInfoProgramInfo getMIInfoProgramInfo() throws MIException {
		return (MIInfoProgramInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoProgramInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
