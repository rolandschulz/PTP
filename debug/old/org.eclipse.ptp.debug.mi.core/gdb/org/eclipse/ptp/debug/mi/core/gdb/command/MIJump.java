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

import org.eclipse.ptp.debug.mi.core.gdb.output.MIOutput;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIResultRecord;


/**
 * 
 *    jump LINESPEC
 *
 */
public class MIJump extends CLICommand {

	MIOutput out;

	public MIJump(String loc) {
		super("jump " + loc); //$NON-NLS-1$
	}

	/**
	 *  This is a CLI command contraly to
	 *  the -exec-continue or -exec-run
	 *  it does not return so we have to fake
	 *  a return value. We return "^running"
	 */
	public MIOutput getMIOutput() {
		if (out == null) {
			out =  new MIOutput();
			MIResultRecord rr = new MIResultRecord();
			rr.setToken(getToken());
			rr.setResultClass(MIResultRecord.RUNNING);
			out.setMIResultRecord(rr);
		}
		return out;
	}

}
