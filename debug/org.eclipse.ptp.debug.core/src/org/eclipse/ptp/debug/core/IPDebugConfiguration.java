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
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface used to represent a debugger configuration.
 * 
 */
public interface IPDebugConfiguration {
	public final static String CPU_NATIVE = "native"; //$NON-NLS-1$

	/**
	 * @return
	 */
	@Deprecated
	public String[] getCoreFileExtensions();

	/**
	 * @return
	 */
	@Deprecated
	public String[] getCPUList();

	/**
	 * Get an instance of the debugger defined by this configuration.
	 * 
	 * @return debugger instance
	 * @throws CoreException
	 */
	public IPDebugger getDebugger() throws CoreException;

	/**
	 * Get the ID of the debugger
	 * 
	 * @return debugger ID
	 */
	public String getID();

	/**
	 * @return
	 */
	public String[] getModeList();

	/**
	 * Get the name of the debugger.
	 * 
	 * @return name of the debugger
	 */
	public String getName();

	/**
	 * @return
	 */
	@Deprecated
	public String getPlatform();

	/**
	 * @param cpu
	 * @return
	 */
	@Deprecated
	public boolean supportsCPU(String cpu);

	/**
	 * Test if the debugger support the debug launch mode (run or attach)
	 * 
	 * @param mode
	 *            debugger launch mode
	 * @return true if the debugger supports the supplied mode
	 */
	public boolean supportsMode(String mode);
}
