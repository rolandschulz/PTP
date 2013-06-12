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
package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Represents a signal
 * 
 * @author clement
 * 
 */
public interface IPDISignal extends IPDISessionObject {
	/**
	 * Returns the meaning of this signal.
	 * 
	 * @return the meaning of this signal
	 */
	public String getDescription();

	/**
	 * Returns the name of this signal.
	 * 
	 * @return the name of this signal
	 */
	public String getName();

	/**
	 * Change the way debugger handles this signal.
	 * 
	 * @param ignore
	 *            - if true the debugger should not allow your program to see this signal
	 * @param stop
	 *            - if true the debugger should stop your program when this signal happens
	 * @throws PDIException
	 *             on failure
	 */
	public void handle(boolean ignore, boolean stop) throws PDIException;

	/**
	 * if false means program will see the signal.
	 * Otherwise program does not know.
	 * 
	 * @return boolean
	 */
	public boolean isIgnore();

	/**
	 * Means reenter debugger if this signal happens
	 * Method isStopSet.
	 * 
	 * @return boolean
	 */
	public boolean isStopSet();

	/**
	 * Set descriptor
	 * 
	 * @param desc
	 */
	public void setDescriptor(IPDISignalDescriptor desc);

	/**
	 * Set handle
	 * 
	 * @param isIgnore
	 * @param isStop
	 */
	public void setHandle(boolean isIgnore, boolean isStop);

	/**
	 * Continue program giving it this signal.
	 * 
	 * @throws PDIException
	 *             on failure
	 */
	public void signal() throws PDIException;
}
