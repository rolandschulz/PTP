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

/**
 * 
 *      -exec-run
 *
 *   Asynchronous command.  Starts execution of the inferior from the
 * beginning.  The inferior executes until either a breakpoint is
 * encountered or the program exits.
 * 
 */
public class MIExecRun extends MICommand 
{
	public MIExecRun() {
		super("-exec-run"); //$NON-NLS-1$
	}
	
	public MIExecRun(String[] args) {
		super("-exec-run", args); //$NON-NLS-1$
	}
}
