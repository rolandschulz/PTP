/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.debug.core;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IPJob;


public interface IPCDIDebugger extends ICDIDebugger {
	public ICDISession createDebuggerSession(IPJob[] jobs, ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException;
	public ICDISession createDebuggerSession(IPJob[] jobs, ILaunch launch, File exe, IProgressMonitor monitor) throws CoreException;
}
