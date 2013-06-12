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

import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * A stack frame in a suspended thread.
 * A stack frame contains variables representing visible locals and arguments at the current execution location.
 * 
 * @author clement
 * 
 */
public interface IPDIStackFrame extends IPDISessionObject {
	/**
	 * Returns the location of the instruction pointer in this stack frame.
	 * 
	 * @return the location of the instruction pointer
	 */
	public IPDILocator getLocator();

	/**
	 * Returns the visible variables in this stack frame. An empty collection is returned if there are no visible variables.
	 * 
	 * @return a collection of visible variables
	 * @throws PDIException
	 *             on failure
	 */
	public IPDILocalVariableDescriptor[] getLocalVariableDescriptors() throws PDIException;

	/**
	 * Create a variable from the descriptor for evaluation. A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically removed from the manager list.
	 * 
	 * @param varDesc
	 *            IPDIArgumentDescriptor
	 * @return argument
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIArgument createArgument(IPDIArgumentDescriptor varDesc) throws PDIException;

	/**
	 * Create a variable from the descriptor for evaluation. A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically removed from the manager list.
	 * 
	 * @param varDesc
	 *            IPDILocalVariableDescriptor
	 * @return local variable
	 * @throws PDIException
	 *             on failure
	 */
	public IPDILocalVariable createLocalVariable(IPDILocalVariableDescriptor varDesc) throws PDIException;

	/**
	 * Returns the arguments in this stack frame. An empty collection is returned if there are no arguments.
	 * 
	 * @return a collection of arguments
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIArgumentDescriptor[] getArgumentDescriptors() throws PDIException;

	/**
	 * Returns the thread this stackframe is contained in.
	 * 
	 * @return the thread
	 */
	public IPDIThread getThread();

	/**
	 * Returns the target this stackframe is contained in.
	 * 
	 * @return the target
	 */
	public IPDITarget getTarget();

	/**
	 * Returns the level of the stack frame, 1 based.
	 * 
	 * @return the level of the stack frame
	 */
	public int getLevel();

	/**
	 * Determines whether both stackframes are the same
	 * 
	 * @param stackframe
	 * @return true if the frames are the same.
	 */
	public boolean equals(IPDIStackFrame stackframe);
}
