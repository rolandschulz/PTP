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
 *    handle SIGUSR1 nostop noignore
 *
 */
public class MIHandle extends CLICommand {

	public MIHandle(String arg) {
		super("handle " + arg); //$NON-NLS-1$
	}

}