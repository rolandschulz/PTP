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
package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Represents a line breakpoint
 * 
 * @author Clement
 * 
 */
public interface IPLineBreakpoint extends IPBreakpoint, ILineBreakpoint {
	public static final String ADDRESS = "org.eclipse.ptp.debug.core.address"; //$NON-NLS-1$
	public static final String FUNCTION = "org.eclipse.ptp.debug.core.function"; //$NON-NLS-1$

	/**
	 * Get the address associated with the breakpoint
	 * 
	 * @return
	 * @throws CoreException
	 */
	public String getAddress() throws CoreException;

	/**
	 * Get the file name
	 * 
	 * @return
	 * @throws CoreException
	 */
	public String getFileName() throws CoreException;

	/**
	 * Get the function
	 * 
	 * @return
	 * @throws CoreException
	 */
	public String getFunction() throws CoreException;

	/**
	 * Set the breakpoint address
	 * 
	 * @param address
	 * @throws CoreException
	 */
	public void setAddress(String address) throws CoreException;

	/**
	 * Set the breakpoint function
	 * 
	 * @param function
	 * @throws CoreException
	 */
	public void setFunction(String function) throws CoreException;
}
