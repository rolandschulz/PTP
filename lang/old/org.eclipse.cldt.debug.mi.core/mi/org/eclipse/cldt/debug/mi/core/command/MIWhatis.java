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

package org.eclipse.cldt.debug.mi.core.command;

import org.eclipse.cldt.debug.mi.core.MIException;
import org.eclipse.cldt.debug.mi.core.output.MIInfo;
import org.eclipse.cldt.debug.mi.core.output.MIOutput;
import org.eclipse.cldt.debug.mi.core.output.MIWhatisInfo;

/**
 * 
 *    whatis type
 *
 */
public class MIWhatis extends CLICommand 
{
	public MIWhatis(String var) {
		super("whatis " + var); //$NON-NLS-1$
	}

	public MIWhatisInfo getMIWhatisInfo() throws MIException {
		return (MIWhatisInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIWhatisInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
