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

package org.eclipse.fdt.debug.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.ICDICondition;
import org.eclipse.fdt.debug.core.cdi.ICDILocation;
import org.eclipse.fdt.debug.core.cdi.ICDISessionObject;

/**
 * 
 * Represents a debuggable process. This is a root object of the CDI
 * model.
 * 
 * @since Jul 8, 2002
 */
public interface ICDITarget extends ICDIThreadGroup, ICDIExpressionManagement, 
	ICDISourceManagement, ICDISharedLibraryManagement, ICDIMemoryBlockManagement, ICDISessionObject {

	/**
	 * Gets the target process.
	 *
	 * @return  the output stream connected to the normal input of the
	 *          target process.
	 */
	Process getProcess();

	/**
	 * Returns the configuration description of this debug session.
	 * 
	 * @return the configuration description
	 */
	ICDITargetConfiguration getConfiguration();

	/**
	 * Evaluates the expression specified by the given string.
	 * Returns the evaluation result as a String.
	 * 
	 * @param - expression string to be evaluated
	 * @return the result of the evaluation
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String evaluateExpressionToString(ICDIStackFrame context, String expressionText)
		throws CDIException;

	/**
	 * A static/global variable in a particular function or file,
	 * filename or/and function is the context for the static ICDIVariableDescriptor.
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
	 * @param filename
	 * @param function
	 * @param name
	 * @return ICDIVariableDescriptor
	 * @throws CDIException
	 */
	ICDIVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws CDIException;

	/**
	 * Return the register groups.
	 * 
	 * @return ICDIRegisterGroup[]
	 */
	ICDIRegisterGroup[] getRegisterGroups() throws CDIException;

	/**
	 * Returns whether this target is terminated.
	 *
	 * @return whether this target is terminated
	 */
	boolean isTerminated();

	/**
	 * Causes this target to terminate.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void terminate() throws CDIException;

	/**
	 * Returns whether this target is disconnected.
	 *
	 * @return whether this target is disconnected
	 */
	boolean isDisconnected();

	/**
	 * Disconnects this target from the debuggable process. Generally, 
	 * disconnecting ends a debug session with this target, but allows 
	 * the debuggable program to continue running.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void disconnect() throws CDIException;

	/**
	 * Restarts the execution of this target.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void restart() throws CDIException;

	/**
	 * Equivalent to resume(false)
	 * 
	 * @deprecated 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void resume() throws CDIException;

	/**
	 * Equivalent to stepOver(1)
	 * 
	 * @deprecated
	 * @see #stepOver(int)
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOver() throws CDIException;

	/**
	 * Equivalent to stepInto(1)
	 * 
	 * @deprecated
	 * @see #stepInto(int) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepInto() throws CDIException;

	/**
	 * Equivalent to stepOverInstruction(1)
	 * 
	 * @deprecated
	 * @see stepOverInstruction(int) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOverInstruction() throws CDIException;

	/**
	 * Equivalent to stepIntoInstruction(1)
	 * 
	 * @deprecated
	 * @see #stepIntoInstruction(int) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepIntoInstruction() throws CDIException;

	/**
	 * Equivaltent to stepUntil(location)
	 * 
	 * @deprecated
	 * @see #stepUntil(ICDILocation) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void runUntil(ICDILocation location) throws CDIException;

	/**
	 * Equivalent to resume(location
	 * 
	 * @deprecated
	 * @see #resume(ICDLocation) 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void jump(ICDILocation location) throws CDIException;
	
	/**
	 * Equivalent to resume(false)
	 * 
	 * @deprecated
	 * @throws CDIException
	 */
	void signal() throws CDIException;

	/**
	 * Equivalent to resume(signal)
	 * 
	 * @deprecated
	 * @see #resume(ICDISignal) 
	 * @param signal
	 * @throws CDIException
	 */
	void signal(ICDISignal signal) throws CDIException;

	/**
	 * Returns the Runtime options for this target debug session.
	 * 
	 * @return the configuration description
	 */
	ICDIRuntimeOptions getRuntimeOptions();

	/**
	 * Return a ICDICondition
	 */
	ICDICondition createCondition(int ignoreCount, String expression);

	/**
	 * Return a ICDICondition
	 */
	ICDICondition createCondition(int ignoreCount, String expression, String[] threadIds);

	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(String file, String function, int line);

	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(BigInteger address);

}
