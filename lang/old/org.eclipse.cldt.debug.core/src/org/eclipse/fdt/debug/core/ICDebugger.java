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
package org.eclipse.fdt.debug.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.ICDISession;

/**
 * 
 * @deprecated - see ICDIDebugger
 */
public interface ICDebugger {
	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException ;
	public ICDISession createAttachSession(ILaunchConfiguration config, IFile exe, int pid) throws CDIException;
	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException;
}
