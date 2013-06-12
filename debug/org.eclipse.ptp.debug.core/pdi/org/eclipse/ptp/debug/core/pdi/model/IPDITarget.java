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
 * Represents a debuggable process. This is a root object of the PDI model.
 * 
 * @author clement
 * 
 */
public interface IPDITarget extends IPDISourceManagement, IPDISharedLibraryManagement, IPDISessionObject {
	/**
	 * Create a variable from the descriptor for evaluation. A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically
	 * removed from the manager list.
	 * 
	 * @param varDesc
	 *            IPDIGlobalVariableDescriptor
	 * @return IPDIGlobalVariable
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIGlobalVariable createGlobalVariable(IPDIGlobalVariableDescriptor varDesc) throws PDIException;

	/**
	 * Create a variable from the descriptor for evaluation. A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically
	 * removed from the manager list.
	 * 
	 * @param varDesc
	 *            IPDThreadStorageDesc
	 * @return IPDIRegister
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIRegister createRegister(IPDIRegisterDescriptor varDesc) throws PDIException;

	/**
	 * Evaluates the expression specified by the given string. Returns the evaluation result as a String.
	 * 
	 * @param - expression string to be evaluated
	 * @return the result of the evaluation
	 * @throws PDIException
	 *             on failure
	 */
	public String evaluateExpressionToString(IPDIStackFrame context, String expressionText) throws PDIException;

	/**
	 * Returns the currently selected thread.
	 * 
	 * @return the currently selected thread
	 */
	public IPDIThread getCurrentThread() throws PDIException;

	/**
	 * A static/global variable in a particular function or file,
	 * filename or/and function is the context for the static IPDIVariableDescriptor.
	 * 
	 * <pre>
	 * hello.c:
	 *   int bar;
	 *   int main() {
	 *   	static int bar;
	 *   }
	 * file.c:
	 *   int foo() {
	 *   	static int bar;
	 *   }
	 * getVariableObject(null, null, "bar");
	 * getVariableObject(null, "main", "bar");
	 * getVariableObject("file.c", "foo", "bar");
	 * </pre>
	 * 
	 * @param filename
	 * @param function
	 * @param name
	 * @return IPDIGlobalVariableDescriptor
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name)
			throws PDIException;

	/**
	 * Return the register groups.
	 * 
	 * @return IPDIRegisterGroup[]
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIRegisterGroup[] getRegisterGroups() throws PDIException;

	/**
	 * Returns the Runtime options for this target debug session.
	 * 
	 * @return the configuration description
	 */
	public IPDIRuntimeOptions getRuntimeOptions();

	/**
	 * Returns the threads contained in this target. An empty collection is returned if this target contains no threads.
	 * 
	 * @return a collection of threads
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIThread[] getThreads() throws PDIException;

	/**
	 * Lock target
	 */
	public void lockTarget();

	/**
	 * Release target
	 */
	public void releaseTarget();

	/**
	 * Set current thread
	 * 
	 * @param pthread
	 * @param doUpdate
	 * @throws PDIException
	 */
	public void setCurrentThread(IPDIThread pthread, boolean doUpdate) throws PDIException;

	/**
	 * Set suspended state
	 * 
	 * @param state
	 */
	public void setSupended(boolean state);

	/**
	 * Update state
	 * 
	 * @param newThreadId
	 */
	public void updateState(int newThreadId);
}
