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
import org.eclipse.ptp.debug.mi.core.gdb.output.MIVarListChildrenInfo;



/**
 * 
 *     -var-list-children NAME
 *
 *  Returns a list of the children of the specified variable object:
 *
 *     numchild=N,children={{name=NAME,
 *     numchild=N,type=TYPE},(repeats N times)}
 * 
 */
public class MIVarListChildren extends MICommand 
{
	public MIVarListChildren(String name) {
		super("-var-list-children", new String[]{name}); //$NON-NLS-1$
	}

	public MIVarListChildrenInfo getMIVarListChildrenInfo() throws MIException {
		return (MIVarListChildrenInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarListChildrenInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}