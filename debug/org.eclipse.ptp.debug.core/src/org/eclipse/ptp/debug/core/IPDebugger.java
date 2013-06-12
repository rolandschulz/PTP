/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDISession;

/**
 * Main debugger interface used by debug launch delegate.
 * 
 */
public interface IPDebugger {
	/**
	 * Clean up the debug session. Does whatever is necessary to shut down any
	 * debugger activities that were started as a result of calling initialize.
	 * 
	 * @param launch
	 *            debugger launch configuration
	 */
	public void cleanup(IPLaunch launch);

	/**
	 * Create a new debugger session.
	 * 
	 * @param timeout
	 *            timeout value for debug commands
	 * @param launch
	 *            debugger launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return new debug session
	 * @throws CoreException
	 * @since 5.0
	 */
	public IPDISession createDebugSession(long timeout, IPLaunch launch, IProgressMonitor monitor) throws CoreException;

	/**
	 * Initialize the debugger. This does whatever is necessary to get the
	 * debugger ready to start debugging the user application. The debugger
	 * should add the required attributes to attrMgr that need to be passed to
	 * the submitJob command to launch the application under debugger control.
	 * The attrMgr argument can also supply attributes from a previously
	 * submitted job that may be used to initialize the debug session.
	 * 
	 * @param configuration
	 *            debugger launch configuration
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             if the debugger cannot be initialized
	 * @since 5.0
	 */
	public void initialize(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException;
}
