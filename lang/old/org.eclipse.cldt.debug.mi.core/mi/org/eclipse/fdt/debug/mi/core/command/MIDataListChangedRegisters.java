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
import org.eclipse.fdt.debug.mi.core.output.MIDataListChangedRegistersInfo;
import org.eclipse.fdt.debug.mi.core.output.MIInfo;
import org.eclipse.fdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -data-list-changed-registers
 *
 *   Display a list of the registers that have changed.
 *
 */
public class MIDataListChangedRegisters extends MICommand 
{
	public MIDataListChangedRegisters() {
		super("-data-list-changed-registers" ); //$NON-NLS-1$
	}

	public MIDataListChangedRegistersInfo getMIDataListChangedRegistersInfo() throws MIException {
		return (MIDataListChangedRegistersInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataListChangedRegistersInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
