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
package org.eclipse.ptp.internal.debug.sdm.core.proxy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.internal.debug.core.PDebugOptions;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.proxy.debug.client.AbstractProxyDebugClient;
import org.eclipse.ptp.proxy.debug.command.IProxyDebugCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugBreakpointAfterCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugCLICommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugConditionBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDataReadMemoryCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDataWriteMemoryCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDeleteBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDeletePartialExpressionCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDisableBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugEnableBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugEvaluateExpressionCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugEvaluatePartialExpressionCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugGetTypeCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugGoCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugInterruptCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugListArgumentsCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugListGlobalVariablesCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugListInfoThreadsCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugListLocalVariablesCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugListSignalsCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugListStackframesCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugSetCurrentStackframeCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugSetFunctionBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugSetLineBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugSetThreadSelectCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugSetWatchpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugStackInfoDepthCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugStartSessionCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugStepCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugTerminateCommand;

public class ProxyDebugClient extends AbstractProxyDebugClient {
	public static final int STEP_INTO = 0;
	public static final int STEP_OVER = 1;
	public static final int STEP_FINISH = 2;

	/**
	 * Convert a proxy representation of a bitset into a TaskSet
	 * 
	 * @param str
	 * @return proxy bitset converted to TaskSet
	 * @since 4.0
	 */
	public static TaskSet decodeTaskSet(String str) {
		String[] parts = str.split(":"); //$NON-NLS-1$
		int len = Integer.parseInt(parts[0], 16); // Skip trailing NULL
		TaskSet b = new TaskSet(len, parts[1]);
		return b;
	}

	/**
	 * Convert a TaskSet to it's proxy representation
	 * 
	 * @param set
	 * @return proxy representation of a TaskSet
	 * @since 4.0
	 */
	public static String encodeTaskSet(TaskSet set) {
		String lenStr = Integer.toHexString(set.taskSize());
		lenStr += ":" + set.toHexString(); //$NON-NLS-1$
		return lenStr;
	}

	/**
	 * @since 4.0
	 */
	public void debugBreakpointAfter(TaskSet procs, int bpid, int icount) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugBreakpointAfterCommand(encodeTaskSet(procs), bpid, icount);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugCLIHandle(TaskSet procs, String arg) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugCLICommand(encodeTaskSet(procs), arg);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugConditionBreakpoint(TaskSet procs, int bpid, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugConditionBreakpointCommand(encodeTaskSet(procs), bpid, expr);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugDeleteBreakpoint(TaskSet procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDeleteBreakpointCommand(encodeTaskSet(procs), bpid);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugDeletePartialExpression(TaskSet procs, String name) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDeletePartialExpressionCommand(encodeTaskSet(procs), name);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugDisableBreakpoint(TaskSet procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDisableBreakpointCommand(encodeTaskSet(procs), bpid);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugEnableBreakpoint(TaskSet procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEnableBreakpointCommand(encodeTaskSet(procs), bpid);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugEvaluateExpression(TaskSet procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEvaluateExpressionCommand(encodeTaskSet(procs), expr);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugEvaluatePartialExpression(TaskSet procs, String name, String exprId, boolean listChildren, boolean express)
			throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEvaluatePartialExpressionCommand(encodeTaskSet(procs), name, exprId, listChildren,
				express);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugGetType(TaskSet procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGetTypeCommand(encodeTaskSet(procs), expr);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugGo(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGoCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugInterrupt(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugInterruptCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugListArguments(TaskSet procs, int low, int high) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListArgumentsCommand(encodeTaskSet(procs), low, high);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugListGlobalVariables(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListGlobalVariablesCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugListInfoThreads(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListInfoThreadsCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugListLocalVariables(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListLocalVariablesCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugListSignals(TaskSet procs, String name) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListSignalsCommand(encodeTaskSet(procs), name);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugListStackframes(TaskSet procs, int low, int high) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListStackframesCommand(encodeTaskSet(procs), low, high);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugSetCurrentStackframe(TaskSet procs, int level) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetCurrentStackframeCommand(encodeTaskSet(procs), level);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugSetFuncBreakpoint(TaskSet procs, int bpid, boolean isTemporary, boolean isHardware, String file, String func,
			String expression, int ignoreCount, int tid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetFunctionBreakpointCommand(encodeTaskSet(procs), bpid, isTemporary, isHardware,
				file, func, expression, ignoreCount, tid);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugSetLineBreakpoint(TaskSet procs, int bpid, boolean isTemporary, boolean isHardware, String file, int line,
			String expression, int ignoreCount, int tid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetLineBreakpointCommand(encodeTaskSet(procs), bpid, isTemporary, isHardware, file,
				line, expression, ignoreCount, tid);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugSetThreadSelect(TaskSet procs, int threadNum) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetThreadSelectCommand(encodeTaskSet(procs), threadNum);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugSetWatchpoint(TaskSet procs, int bpid, String expression, boolean isAccess, boolean isRead, String condition,
			int ignoreCount) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetWatchpointCommand(encodeTaskSet(procs), bpid, expression, isAccess, isRead,
				condition, ignoreCount);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugStackInfoDepth(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStackInfoDepthCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugStartSession(String prog, String path, String dir, String[] args) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStartSessionCommand(prog, path, dir, args);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugStep(TaskSet procs, int count, int type) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStepCommand(encodeTaskSet(procs), count, type);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void debugTerminate(TaskSet procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugTerminateCommand(encodeTaskSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void setDataReadMemoryCommand(TaskSet procs, long offset, String address, String format, int wordSize, int rows,
			int cols, Character asChar) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDataReadMemoryCommand(encodeTaskSet(procs), offset, address, format, wordSize, rows,
				cols, asChar);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * @since 4.0
	 */
	public void setDataWriteMemoryCommand(TaskSet procs, long offset, String address, String format, int wordSize, String value)
			throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDataWriteMemoryCommand(encodeTaskSet(procs), offset, address, format, wordSize,
				value);
		sendCommand(cmd);
		cmd.completed();
	}

	/**
	 * Wait for the debugger to connect. We will receive a connected event when
	 * the debug server actually connects, or a timeout event if the server
	 * fails to connect.
	 * 
	 * @param monitor
	 * @return true on successful connection, false otherwise
	 * @throws IOException
	 */
	public boolean waitConnect(IProgressMonitor monitor) throws IOException {
		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.ProxyDebugClient_0);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		waitLock.lock();
		try {
			if (state == DebugProxyState.CONNECTING) {
				while (state == DebugProxyState.CONNECTING && !timeout && !monitor.isCanceled()) {
					waiting = true;
					try {
						// added to wait for 1000
						waitCondition.await(1000, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
						monitor.setCanceled(true);
					}
				}
				if (timeout) {
					throw new IOException(Messages.ProxyDebugClient_1);
				}
				if (monitor.isCanceled()) {
					return false;
				}
				if (state != DebugProxyState.CONNECTED) {
					return false;
				}
			}
			return true;
		} finally {
			waitLock.unlock();
			monitor.done();
		}
	}
}
