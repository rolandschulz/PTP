/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.model;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;

/**
 * Factory for create the PDI model elements
 * 
 */
public interface IPDIModelFactory {
	/**
	 * Create an address breakpoint
	 * 
	 * @param session
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param enabled
	 * @return
	 * @since 4.0
	 */
	public IPDIAddressBreakpoint newAddressBreakpoint(IPDISession session, TaskSet tasks, int type, IPDILocation location,
			IPDICondition condition, boolean enabled);

	/**
	 * Create an argument
	 * 
	 * @param session
	 * @param argDesc
	 * @param varId
	 * @return
	 */
	public IPDIArgument newArgument(IPDISession session, IPDIArgumentDescriptor argDesc, String varId);

	/**
	 * Create an argument descriptor
	 * 
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 * @since 4.0
	 */
	public IPDIArgumentDescriptor newArgumentDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos, int depth);

	/**
	 * Create a condition
	 * 
	 * @param ignore
	 * @param exp
	 * @param ids
	 * @return
	 */
	public IPDICondition newCondition(int ignore, String exp, String[] ids);

	/**
	 * Create an exception point
	 * 
	 * @param session
	 * @param tasks
	 * @param clazz
	 * @param stopOnThrow
	 * @param stopOnCatch
	 * @param condition
	 * @param enabled
	 * @param funcBpts
	 * @return
	 * @since 4.0
	 */
	public IPDIExceptionpoint newExceptionpoint(IPDISession session, TaskSet tasks, String clazz, boolean stopOnThrow,
			boolean stopOnCatch, IPDICondition condition, boolean enabled, IPDIFunctionBreakpoint[] funcBpts);

	/**
	 * Create an expression
	 * 
	 * @param session
	 * @param tasks
	 * @param ex
	 * @return
	 * @since 4.0
	 */
	public IPDITargetExpression newExpression(IPDISession session, TaskSet tasks, String ex);

	/**
	 * Create a function breakpoint
	 * 
	 * @param session
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param enabled
	 * @return
	 * @since 4.0
	 */
	public IPDIFunctionBreakpoint newFunctionBreakpoint(IPDISession session, TaskSet tasks, int type, IPDILocation location,
			IPDICondition condition, boolean enabled);

	/**
	 * Create a global variable
	 * 
	 * @param session
	 * @param varDesc
	 * @param varId
	 * @return
	 */
	public IPDIGlobalVariable newGlobalVariable(IPDISession session, IPDIGlobalVariableDescriptor varDesc, String varId);

	/**
	 * Create a global variable descriptor
	 * 
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 * @since 4.0
	 */
	public IPDIGlobalVariableDescriptor newGlobalVariableDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos, int depth);

	/**
	 * Create a line breakpoint
	 * 
	 * @param session
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param enabled
	 * @return
	 * @since 4.0
	 */
	public IPDILineBreakpoint newLineBreakpoint(IPDISession session, TaskSet tasks, int type, IPDILocation location,
			IPDICondition condition, boolean enabled);

	/**
	 * Create a local variable
	 * 
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @param varId
	 * @since 4.0
	 */
	public IPDILocalVariable newLocalVariable(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame,
			String name, String fullName, int pos, int depth, String varid);

	/**
	 * Create a local variable
	 * 
	 * @param session
	 * @param varDesc
	 * @param varId
	 * @return
	 */
	public IPDILocalVariable newLocalVariable(IPDISession session, IPDILocalVariableDescriptor varDesc, String varId);

	/**
	 * Create a local variable descriptor
	 * 
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 * @since 4.0
	 */
	public IPDILocalVariableDescriptor newLocalVariableDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos, int depth);

	/**
	 * Create a memory block
	 * 
	 * @param address
	 * @param ascii
	 * @param data
	 * @return
	 */
	public IPDIMemory newMemory(String address, String ascii, String[] data);

	/**
	 * Create a memory block
	 * 
	 * @param session
	 * @param tasks
	 * @param exp
	 * @param wordSize
	 * @param b
	 * @param info
	 * @return
	 * @since 4.0
	 */
	public IPDIMemoryBlock newMemoryBlock(IPDISession session, TaskSet tasks, String exp, int wordSize, boolean b,
			IPDIDataReadMemoryInfo info);

	/**
	 * Create a multi expression
	 * 
	 * @param session
	 * @param tasks
	 * @param ex
	 * @param enabled
	 * @return
	 * @since 4.0
	 */
	public IPDIMultiExpressions newMultiExpressions(IPDISession session, TaskSet tasks, String ex, boolean enabled);

	/**
	 * Create a register descriptor
	 * 
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 * @since 4.0
	 */
	public IPDIVariableDescriptor newRegisterDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos, int depth);

	/**
	 * Create a signal handler
	 * 
	 * @param session
	 * @param tasks
	 * @param signalDescriptor
	 * @return
	 * @since 4.0
	 */
	public IPDISignal newSignal(IPDISession session, TaskSet tasks, IPDISignalDescriptor signalDescriptor);

	/**
	 * Create a signal descriptor
	 * 
	 * @param name
	 * @param stop
	 * @param pass
	 * @param print
	 * @param description
	 * @return
	 */
	public IPDISignalDescriptor newSignalDescriptor(String name, boolean stop, boolean pass, boolean print, String description);

	/**
	 * Create a stack frame from a locator
	 * 
	 * @param session
	 * @param pthread
	 * @param i
	 * @param locator
	 * @return
	 */
	public IPDIStackFrame newStackFrame(IPDISession session, IPDIThread pthread, int i, IPDILocator locator);

	/**
	 * Create a stack frame
	 * 
	 * @param session
	 * @param thread
	 * @param level
	 * @param file
	 * @param func
	 * @param line
	 * @param addr
	 * @param args
	 * @return
	 */
	public IPDIStackFrame newStackFrame(IPDISession session, IPDIThread thread, int level, String file, String func, int line,
			BigInteger addr);

	/**
	 * Create a stack frame descriptor
	 * 
	 * @param level
	 * @param loc
	 * @return
	 */
	public IPDIStackFrameDescriptor newStackFrameDescriptor(int level, IPDILocator loc);

	/**
	 * Create a target
	 * 
	 * @param session
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDITarget newTarget(IPDISession session, TaskSet tasks);

	/**
	 * Create a thread
	 * 
	 * @param session
	 * @param target
	 * @param parseInt
	 * @return
	 */
	public IPDIThread newThread(IPDISession session, IPDITarget target, int parseInt);

	/**
	 * Create a thread storage descriptor
	 * 
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 * @since 4.0
	 */
	public IPDIVariableDescriptor newThreadStorageDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos, int depth);

	/**
	 * Create a watchpoint
	 * 
	 * @param session
	 * @param tasks
	 * @param type
	 * @param expression
	 * @param wType
	 * @param condition
	 * @param enabled
	 * @return
	 * @since 4.0
	 */
	public IPDIWatchpoint newWatchpoint(IPDISession session, TaskSet tasks, int type, String expression, int wType,
			IPDICondition condition, boolean enabled);
}
