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

package org.eclipse.fdt.debug.mi.core.command;

import org.eclipse.fdt.debug.mi.core.MIException;
import org.eclipse.fdt.debug.mi.core.output.MIInfo;
import org.eclipse.fdt.debug.mi.core.output.MIOutput;
import org.eclipse.fdt.debug.mi.core.output.MIStackInfoDepthInfo;

/**
 * 
 *     -stack-info-depth [ MAX-DEPTH ]
 *
 *  Return the depth of the stack.  If the integer argument MAX-DEPTH is
 *  specified, do not count beyond MAX-DEPTH frames.
 * 
 */
public class MIStackInfoDepth extends MICommand 
{
	public MIStackInfoDepth() {
		super("-stack-info-depth"); //$NON-NLS-1$
	}

	public MIStackInfoDepth(int maxDepth) {
		super("-stack-info-depth", new String[]{Integer.toString(maxDepth)}); //$NON-NLS-1$
	}

	public MIStackInfoDepthInfo getMIStackInfoDepthInfo() throws MIException {
		return (MIStackInfoDepthInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIStackInfoDepthInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
