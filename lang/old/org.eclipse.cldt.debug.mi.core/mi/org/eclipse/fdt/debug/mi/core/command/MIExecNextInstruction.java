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

/**
 * 
 *      -exec-next-instruction
 *
 *   Asynchronous command.  Executes one machine instruction.  If the
 * instruction is a function call continues until the function returns.  If
 * the program stops at an instruction in the middle of a source line, the
 * address will be printed as well.
 * 
 */
public class MIExecNextInstruction extends MICommand 
{
	public MIExecNextInstruction() {
		super("-exec-next-instruction"); //$NON-NLS-1$
	}

	public MIExecNextInstruction(int count) {
		super("-exec-next-instruction", new String[] { Integer.toString(count) }); //$NON-NLS-1$
	}
}
