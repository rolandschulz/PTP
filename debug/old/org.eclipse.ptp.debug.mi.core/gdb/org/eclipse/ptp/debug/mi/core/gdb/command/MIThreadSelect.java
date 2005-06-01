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
import org.eclipse.ptp.debug.mi.core.gdb.output.MIOutput;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIThreadSelectInfo;



/**
 * 
 *    -thread-select THREADNUM
 *
 * Make THREADNUM the current thread.  It prints the number of the new
 * current thread, and the topmost frame for that thread.
 * 
 */
public class MIThreadSelect extends MICommand 
{
	public MIThreadSelect(int threadNum) {
		super("-thread-select", new String[]{Integer.toString(threadNum)}); //$NON-NLS-1$
	}

	public MIThreadSelectInfo getMIThreadSelectInfo() throws MIException {
		return (MIThreadSelectInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIThreadSelectInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
