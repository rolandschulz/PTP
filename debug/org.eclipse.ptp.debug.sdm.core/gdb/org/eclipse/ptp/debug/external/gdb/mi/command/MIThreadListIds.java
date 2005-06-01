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
import org.eclipse.ptp.debug.external.gdb.mi.output.MIThreadListIdsInfo;




/**
 * 
 *    -thread-list-ids
 *
 * Produces a list of the currently known GDB thread ids.  At the end
 * of the list it also prints the total number of such threads.
 * 
 */
public class MIThreadListIds extends MICommand 
{
	public MIThreadListIds() {
		super("-thread-list-ids"); //$NON-NLS-1$
	}

	public MIThreadListIdsInfo getMIThreadListIdsInfo() throws MIException {
		return (MIThreadListIdsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIThreadListIdsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
