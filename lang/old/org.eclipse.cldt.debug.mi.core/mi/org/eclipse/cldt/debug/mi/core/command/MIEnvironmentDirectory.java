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
 *      -environment-directory PATHDIR
 *
 *   Add directory PATHDIR to beginning of search path for source files.
 * 
 */
public class MIEnvironmentDirectory extends MICommand 
{
	public MIEnvironmentDirectory(String[] paths) {
		super("-environment-directory", paths); //$NON-NLS-1$
	}

}
