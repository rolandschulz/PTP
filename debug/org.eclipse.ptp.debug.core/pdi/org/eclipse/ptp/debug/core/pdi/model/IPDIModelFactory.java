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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;


public interface IPDIModelFactory {
	/**
	 * @param session
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param enabled
	 * @return
	 */
	public IPDIAddressBreakpoint newAddressBreakpoint(IPDISession session, BitList tasks, int type, 
			IPDILocation location, IPDICondition condition, boolean enabled);
	
	/**
	 * @param session
	 * @param argDesc
	 * @param varId
	 * @return
	 */
	public IPDIArgument newArgument(IPDISession session,
			IPDIArgumentDescriptor argDesc, String varId);
	
	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 */
	public IPDIArgumentDescriptor newArgumentDescriptor(IPDISession session,
			BitList tasks, IPDIThread thread, IPDIStackFrame frame, String name,
			String fullName, int pos, int depth);

	/**
	 * @param ignore
	 * @param exp
	 * @param ids
	 * @return
	 */
	public IPDICondition newCondition(int ignore, String exp, String[] ids);

	/**
	 * @param session
	 * @param tasks
	 * @param clazz
	 * @param stopOnThrow
	 * @param stopOnCatch
	 * @param condition
	 * @param enabled
	 * @param funcBpts
	 * @return
	 */
	public IPDIExceptionpoint newExceptionpoint(IPDISession session, BitList tasks, String clazz, boolean stopOnThrow, 
			boolean stopOnCatch, IPDICondition condition, boolean enabled, IPDIFunctionBreakpoint[] funcBpts);
	
	/**
	 * @param session
	 * @param tasks
	 * @param ex
	 * @return
	 */
	public IPDITargetExpression newExpression(IPDISession session, BitList tasks, String ex);
	
	/**
	 * @param session
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param enabled
	 * @return
	 */
	public IPDIFunctionBreakpoint newFunctionBreakpoint(IPDISession session, BitList tasks, int type, 
			IPDILocation location, IPDICondition condition, boolean enabled);
	
	/**
	 * @param session
	 * @param varDesc
	 * @param varId
	 * @return
	 */
	public IPDIGlobalVariable newGlobalVariable(IPDISession session,
			IPDIGlobalVariableDescriptor varDesc, String varId);
	
	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 */
	public IPDIGlobalVariableDescriptor newGlobalVariableDescriptor(
			IPDISession session, BitList tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos,
			int depth);
	
	/**
	 * @param session
	 * @param tasks
	 * @param type
	 * @param location
	 * @param condition
	 * @param enabled
	 * @return
	 */
	public IPDILineBreakpoint newLineBreakpoint(IPDISession session, BitList tasks, int type, 
			IPDILocation location, IPDICondition condition, boolean enabled);
	
	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @param varId
	 */
	public IPDILocalVariable newLocalVariable(IPDISession session, BitList tasks, IPDIThread thread, IPDIStackFrame frame, String name, String fullName, int pos, int depth, String varid);
	
	/**
	 * @param session
	 * @param varDesc
	 * @param varId
	 * @return
	 */
	public IPDILocalVariable newLocalVariable(IPDISession session,
			IPDILocalVariableDescriptor varDesc, String varId);

	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 */
	public IPDILocalVariableDescriptor newLocalVariableDescriptor(IPDISession session,
			BitList tasks, IPDIThread thread, IPDIStackFrame frame, String name,
			String fullName, int pos, int depth);

	/**
	 * @param address
	 * @param ascii
	 * @param data
	 * @return
	 */
	public IPDIMemory newMemory(String address, String ascii, String[] data);

	/**
	 * @param session
	 * @param tasks
	 * @param exp
	 * @param wordSize
	 * @param b
	 * @param info
	 * @return
	 */
	public IPDIMemoryBlock newMemoryBlock(IPDISession session, BitList tasks,
			String exp, int wordSize, boolean b, IPDIDataReadMemoryInfo info);

	/**
	 * @param session
	 * @param tasks
	 * @param ex
	 * @param enabled
	 * @return
	 */
	public IPDIMultiExpressions newMultiExpressions(IPDISession session, BitList tasks, String ex, boolean enabled);

	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 */
	public IPDIVariableDescriptor newRegisterDescriptor(IPDISession session,
			BitList tasks, IPDIThread thread, IPDIStackFrame frame,
			String name, String fullName, int pos, int depth);

	/**
	 * @param session
	 * @param tasks
	 * @param signalDescriptor
	 * @return
	 */
	public IPDISignal newSignal(IPDISession session, BitList tasks, IPDISignalDescriptor signalDescriptor);


	/**
	 * @param name
	 * @param stop
	 * @param pass
	 * @param print
	 * @param description
	 * @return
	 */
	public IPDISignalDescriptor newSignalDescriptor(String name, boolean stop,
			boolean pass, boolean print, String description);

	/**
	 * @param session
	 * @param pthread
	 * @param i
	 * @param locator
	 * @return
	 */
	public IPDIStackFrame newStackFrame(IPDISession session, IPDIThread pthread, int i, IPDILocator locator);

	/**
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
	public IPDIStackFrame newStackFrame(IPDISession session, IPDIThread thread, int level, String file, String func, 
			int line, BigInteger addr);

	/**
	 * @param level
	 * @param loc
	 * @return
	 */
	public IPDIStackFrameDescriptor newStackFrameDescriptor(int level, IPDILocator loc);

	/**
	 * @param session
	 * @param tasks
	 * @return
	 */
	public IPDITarget newTarget(IPDISession session, BitList tasks);

	/**
	 * @param session
	 * @param target
	 * @param parseInt
	 * @return
	 */
	public IPDIThread newThread(IPDISession session, IPDITarget target, int parseInt);

	/**
	 * @param session
	 * @param tasks
	 * @param thread
	 * @param frame
	 * @param name
	 * @param fullName
	 * @param pos
	 * @param depth
	 * @return
	 */
	public IPDIVariableDescriptor newThreadStorageDescriptor(
			IPDISession session, BitList tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos,
			int depth);

	/**
	 * @param session
	 * @param tasks
	 * @param type
	 * @param expression
	 * @param wType
	 * @param condition
	 * @param enabled
	 * @return
	 */
	public IPDIWatchpoint newWatchpoint(IPDISession session, BitList tasks, int type, String expression, 
			int wType, IPDICondition condition, boolean enabled);
}
