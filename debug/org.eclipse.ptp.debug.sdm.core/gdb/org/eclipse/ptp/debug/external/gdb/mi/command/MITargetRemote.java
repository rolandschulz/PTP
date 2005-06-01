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
 *
 *     -target-remote host port
 *
 *  Attach to a remote process (gdbserver).
 * 
 */
public class MITargetRemote extends CLICommand 
{
	public MITargetRemote(String host, int port) {
		super("target remote " + host + ":" + Integer.toString(port)); //$NON-NLS-1$
	}
}
