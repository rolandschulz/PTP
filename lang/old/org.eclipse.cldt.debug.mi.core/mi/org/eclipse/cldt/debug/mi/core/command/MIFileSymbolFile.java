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

/**
 * 
 *      -file-symbol-file FILE
 *
 *  Read symbol table info from the specified FILE argument.  When used
 * without arguments, clears GDB's symbol table info.  No output is
 * produced, except for a completion notification.
 * 
 */
public class MIFileSymbolFile extends MICommand 
{
	public MIFileSymbolFile(String file) {
		super("-file-symbol-file", new String[]{file}); //$NON-NLS-1$
	}
}
