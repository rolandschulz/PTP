/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.MICommand;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellMISPUSignalInfo extends MICommand {

	/**
	 * 
	 */
	public CellMISPUSignalInfo(String miVersion) {
		super(miVersion, "-spu-info-signal"); //$NON-NLS-1$
	}
	
	public CellMISPUSignalInfoInfo getMIThreadListIdsInfo() throws MIException {
		return (CellMISPUSignalInfoInfo)getMIInfo();
	}
	
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new CellMISPUSignalInfoInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
