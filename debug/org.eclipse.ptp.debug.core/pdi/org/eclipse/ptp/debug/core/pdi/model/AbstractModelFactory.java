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
import org.eclipse.ptp.debug.internal.core.pdi.model.AddressBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.Argument;
import org.eclipse.ptp.debug.internal.core.pdi.model.ArgumentDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Condition;
import org.eclipse.ptp.debug.internal.core.pdi.model.Exceptionpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.Expression;
import org.eclipse.ptp.debug.internal.core.pdi.model.FunctionBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.GlobalVariable;
import org.eclipse.ptp.debug.internal.core.pdi.model.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.LineBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.model.LocalVariable;
import org.eclipse.ptp.debug.internal.core.pdi.model.LocalVariableDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Memory;
import org.eclipse.ptp.debug.internal.core.pdi.model.MemoryBlock;
import org.eclipse.ptp.debug.internal.core.pdi.model.MultiExpressions;
import org.eclipse.ptp.debug.internal.core.pdi.model.RegisterDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Signal;
import org.eclipse.ptp.debug.internal.core.pdi.model.SignalDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame;
import org.eclipse.ptp.debug.internal.core.pdi.model.StackFrameDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;
import org.eclipse.ptp.debug.internal.core.pdi.model.Thread;
import org.eclipse.ptp.debug.internal.core.pdi.model.ThreadStorageDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Watchpoint;


public abstract class AbstractModelFactory implements IPDIModelFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newAddressBreakpoint(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.IPDILocation, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean)
	 */
	public IPDIAddressBreakpoint newAddressBreakpoint(IPDISession session,
			BitList tasks, int type, IPDILocation location,
			IPDICondition condition, boolean enabled) {
		return new AddressBreakpoint(session, tasks, type, location, condition, enabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newArgument(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor, java.lang.String)
	 */
	public IPDIArgument newArgument(IPDISession session,
			IPDIArgumentDescriptor argDesc, String varId) {
		return new Argument(session, argDesc, varId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newArgumentDescriptor(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int)
	 */
	public IPDIArgumentDescriptor newArgumentDescriptor(IPDISession session,
			BitList tasks, IPDIThread thread, IPDIStackFrame frame,
			String name, String fullName, int pos, int depth) {
		return new ArgumentDescriptor(session, tasks, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newCondition(int, java.lang.String, java.lang.String[])
	 */
	public IPDICondition newCondition(int ignore, String exp, String[] ids) {
		return new Condition(ignore, exp, ids);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newExceptionpoint(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String, boolean, boolean, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean, org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint[])
	 */
	public IPDIExceptionpoint newExceptionpoint(IPDISession session,
			BitList tasks, String clazz, boolean stopOnThrow,
			boolean stopOnCatch, IPDICondition condition, boolean enabled,
			IPDIFunctionBreakpoint[] funcBpts) {
		return new Exceptionpoint(session, tasks, clazz, stopOnThrow, stopOnCatch, condition, enabled, funcBpts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newExpression(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDITargetExpression newExpression(IPDISession session,
			BitList tasks, String ex) {
		return new Expression(session, tasks, ex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newFunctionBreakpoint(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.IPDILocation, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean)
	 */
	public IPDIFunctionBreakpoint newFunctionBreakpoint(IPDISession session,
			BitList tasks, int type, IPDILocation location,
			IPDICondition condition, boolean enabled) {
		return new FunctionBreakpoint(session, tasks, type, location, condition, enabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newGlobalVariable(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariableDescriptor, java.lang.String)
	 */
	public IPDIGlobalVariable newGlobalVariable(IPDISession session,
			IPDIGlobalVariableDescriptor varDesc, String varId) {
		return new GlobalVariable(session, varDesc, varId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newGlobalVariableDescriptor(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int)
	 */
	public IPDIGlobalVariableDescriptor newGlobalVariableDescriptor(
			IPDISession session, BitList tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos,
			int depth) {
		return new GlobalVariableDescriptor(session, tasks, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newLineBreakpoint(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, org.eclipse.ptp.debug.core.pdi.IPDILocation, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean)
	 */
	public IPDILineBreakpoint newLineBreakpoint(IPDISession session,
			BitList tasks, int type, IPDILocation location,
			IPDICondition condition, boolean enabled) {
		return new LineBreakpoint(session, tasks, type, location, condition, enabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newLocalVariable(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int, java.lang.String)
	 */
	public IPDILocalVariable newLocalVariable(IPDISession session,
			BitList tasks, IPDIThread thread, IPDIStackFrame frame,
			String name, String fullName, int pos, int depth, String varid) {
		return new LocalVariable(session, tasks, thread, frame, name, fullName, pos, depth, varid);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newLocalVariable(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariableDescriptor, java.lang.String)
	 */
	public IPDILocalVariable newLocalVariable(IPDISession session,
			IPDILocalVariableDescriptor varDesc, String varId) {
		return new LocalVariable(session, varDesc, varId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newLocalVariableDescriptor(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int)
	 */
	public IPDILocalVariableDescriptor newLocalVariableDescriptor(
			IPDISession session, BitList tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos,
			int depth) {
		return new LocalVariableDescriptor(session, tasks, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newMemory(java.lang.String, java.lang.String, java.lang.String[])
	 */
	public IPDIMemory newMemory(String address, String ascii, String[] data) {
		return new Memory(address, ascii, data);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newMemoryBlock(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String, int, boolean, org.eclipse.ptp.debug.core.pdi.model.IPDIDataReadMemoryInfo)
	 */
	public IPDIMemoryBlock newMemoryBlock(IPDISession session, BitList tasks,
			String exp, int wordSize, boolean b, IPDIDataReadMemoryInfo info) {
		return new MemoryBlock(session, tasks, exp, wordSize, b, info);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newMultiExpressions(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String, boolean)
	 */
	public IPDIMultiExpressions newMultiExpressions(IPDISession session,
			BitList tasks, String ex, boolean enabled) {
		return new MultiExpressions(session, tasks, ex, enabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newRegisterDescriptor(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int)
	 */
	public IPDIVariableDescriptor newRegisterDescriptor(IPDISession session,
			BitList tasks, IPDIThread thread, IPDIStackFrame frame,
			String name, String fullName, int pos, int depth) {
		return new RegisterDescriptor(session, tasks, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newSignal(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor)
	 */
	public IPDISignal newSignal(IPDISession session, BitList tasks,
			IPDISignalDescriptor signalDescriptor) {
		return new Signal(session, tasks, signalDescriptor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newSignalDescriptor(java.lang.String, boolean, boolean, boolean, java.lang.String)
	 */
	public IPDISignalDescriptor newSignalDescriptor(String name, boolean stop,
			boolean pass, boolean print, String description) {
		return new SignalDescriptor(name, stop, pass, print, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newStackFrame(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, int, org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	public IPDIStackFrame newStackFrame(IPDISession session,
			IPDIThread pthread, int i, IPDILocator locator) {
		return new StackFrame(session, pthread, i, locator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newStackFrame(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, int, java.lang.String, java.lang.String, int, java.math.BigInteger)
	 */
	public IPDIStackFrame newStackFrame(IPDISession session, IPDIThread thread,
			int level, String file, String func, int line, BigInteger addr) {
		return new StackFrame(session, thread, level, file, func, line, addr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newStackFrameDescriptor(int, org.eclipse.ptp.debug.core.pdi.IPDILocator)
	 */
	public IPDIStackFrameDescriptor newStackFrameDescriptor(int level,
			IPDILocator loc) {
		return new StackFrameDescriptor(level, loc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newTarget(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList)
	 */
	public IPDITarget newTarget(IPDISession session, BitList tasks) {
		return new Target(session, tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newThread(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.debug.core.pdi.model.IPDITarget, int)
	 */
	public IPDIThread newThread(IPDISession session, IPDITarget target,
			int parseInt) {
		return new Thread(session, target, parseInt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newThreadStorageDescriptor(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIThread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int)
	 */
	public IPDIVariableDescriptor newThreadStorageDescriptor(
			IPDISession session, BitList tasks, IPDIThread thread,
			IPDIStackFrame frame, String name, String fullName, int pos,
			int depth) {
		return new ThreadStorageDescriptor(session, tasks, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory#newWatchpoint(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, java.lang.String, int, org.eclipse.ptp.debug.core.pdi.IPDICondition, boolean)
	 */
	public IPDIWatchpoint newWatchpoint(IPDISession session, BitList tasks,
			int type, String expression, int type2, IPDICondition condition,
			boolean enabled) {
		return new Watchpoint(session, tasks, type, expression, type2, condition, enabled);
	}
}
