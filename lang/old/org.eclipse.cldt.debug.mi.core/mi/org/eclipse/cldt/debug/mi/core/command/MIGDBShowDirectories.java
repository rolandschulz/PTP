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
import org.eclipse.cldt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cldt.debug.mi.core.output.MIInfo;
import org.eclipse.cldt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -gdb-show directories
 *
 *   Show the current value of a GDB variable(directories).
 * 
 */
public class MIGDBShowDirectories extends MIGDBShow {
	public MIGDBShowDirectories() {
		super(new String[] { "directories" }); //$NON-NLS-1$
	}

	public MIGDBShowDirectoriesInfo getMIGDBShowDirectoriesInfo() throws MIException {
		return (MIGDBShowDirectoriesInfo)getMIInfo();
	}
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowDirectoriesInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}