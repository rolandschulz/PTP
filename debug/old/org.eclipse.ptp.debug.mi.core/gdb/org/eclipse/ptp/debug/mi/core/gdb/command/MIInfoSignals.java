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
import org.eclipse.ptp.debug.mi.core.gdb.output.MIInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIInfoSignalsInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIOutput;



/**
 * 
 *    info threads
 *
 */
public class MIInfoSignals extends CLICommand 
{
	public MIInfoSignals() {
		super("info signals"); //$NON-NLS-1$
	}

	public MIInfoSignals(String name) {
		super("info signal " + name); //$NON-NLS-1$
	}

	public MIInfoSignalsInfo getMIInfoSignalsInfo() throws MIException {
		return (MIInfoSignalsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoSignalsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
