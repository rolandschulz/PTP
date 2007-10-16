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

package org.eclipse.ptp.debug.external.core.proxy;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.proxy.debug.client.AbstractProxyDebugClient;
import org.eclipse.ptp.proxy.debug.command.IProxyDebugCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugBreakpointAfterCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugCLICommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugConditionBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDataReadMemoryCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDataWriteMemoryCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDeleteBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugDisableBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugEnableBreakpointCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugEvaluateExpressionCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugGetPartialAIFCommand;
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
import org.eclipse.ptp.proxy.debug.command.ProxyDebugSignalInfoCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugStackInfoDepthCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugStartSessionCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugStepCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugTerminateCommand;
import org.eclipse.ptp.proxy.debug.command.ProxyDebugVariableDeleteCommand;

public class ProxyDebugClient extends AbstractProxyDebugClient {
	public static final int STEP_INTO = 0;
	public static final int STEP_OVER = 1;
	public static final int STEP_FINISH = 2;
	
	/**
	 * Convert a proxy representation of a bitset into a BitList
	 * 
	 * @param str
	 * @return proxy bitset converted to BitList
	 */
	public static BitList decodeBitSet(String str) {
		String[] parts = str.split(":");
		int len = Integer.parseInt(parts[0], 16); // Skip trailing NULL
		return new BitList(len, parts[1]);
	}
	
	/**
	 * Convert a BitList to it's proxy representation
	 * 
	 * @param set
	 * @return proxy representation of a BitList
	 */
	public static String encodeBitSet(BitList set) {
		String lenStr = Integer.toHexString(set.size());
		return lenStr + ":" + set.toString();
	}
	
	/**
	 * Wait for the debugger to connect. We will receive a connected event
	 * when the debug server actually connects, or a timeout event if the server
	 * fails to connect. If the user cancels the wait, we call doShutdown() to
	 * terminate the debugger connection.
	 * 
	 * @param monitor
	 * @return true on successful connection, false otherwise
	 * @throws IOException
	 */
	public boolean waitConnect(IProgressMonitor monitor) throws IOException {
		System.out.println("debug: waiting for connect");
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		waitLock.lock();
		try {
			if (state == DebugProxyState.CONNECTING) {
				while (state != DebugProxyState.CONNECTED && !timeout && !monitor.isCanceled()) {
					waiting = true;
					try {
						waitCondition.await();
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
					}
				}
				if (timeout) {
					throw new IOException("Timeout waiting for debugger to connect");
				} else if (monitor.isCanceled()) {
					doShutdown();
					return false;
				}
			}
			return true;
		} finally {
			waitLock.unlock();
			monitor.done();
		}
	}

	public void debugStartSession(String prog, String path, String dir, String[] args) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStartSessionCommand(prog, path, dir, args);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugSetLineBreakpoint(BitList procs, int bpid, boolean isTemporary, boolean isHardware, String file, int line, String expression, int ignoreCount, int tid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetLineBreakpointCommand(encodeBitSet(procs), bpid, isTemporary, isHardware, file, line, expression, ignoreCount, tid);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugSetFuncBreakpoint(BitList procs, int bpid, boolean isTemporary, boolean isHardware, String file, String func, String expression, int ignoreCount, int tid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetFunctionBreakpointCommand(encodeBitSet(procs), bpid, isTemporary, isHardware, file, func, expression, ignoreCount, tid);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugSetWatchpoint(BitList procs, int bpid, String expression, boolean isAccess, boolean isRead, String condition, int ignoreCount) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetWatchpointCommand(encodeBitSet(procs), bpid, expression, isAccess, isRead, condition, ignoreCount);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugDeleteBreakpoint(BitList procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDeleteBreakpointCommand(encodeBitSet(procs), bpid);
		sendCommand(cmd);
	}

	public void debugEnableBreakpoint(BitList procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEnableBreakpointCommand(encodeBitSet(procs), bpid);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugDisableBreakpoint(BitList procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDisableBreakpointCommand(encodeBitSet(procs), bpid);
		sendCommand(cmd);
	}

	public void debugConditionBreakpoint(BitList procs, int bpid, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugConditionBreakpointCommand(encodeBitSet(procs), bpid, expr);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugBreakpointAfter(BitList procs, int bpid, int icount) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugBreakpointAfterCommand(encodeBitSet(procs), bpid, icount);
		sendCommand(cmd);
	}

	public void debugStackInfoDepth(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStackInfoDepthCommand(encodeBitSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugGo(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGoCommand(encodeBitSet(procs));
		sendCommand(cmd);
	}
	
	public void debugStep(BitList procs, int count, int type) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStepCommand(encodeBitSet(procs), count, type);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugTerminate(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugTerminateCommand(encodeBitSet(procs));
		sendCommand(cmd);
	}
	
	public void debugInterrupt(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugInterruptCommand(encodeBitSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugListStackframes(BitList procs, int low, int high) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListStackframesCommand(encodeBitSet(procs), low, high);
		sendCommand(cmd);
	}

	public void debugSetCurrentStackframe(BitList procs, int level) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetCurrentStackframeCommand(encodeBitSet(procs), level);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugEvaluateExpression(BitList procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEvaluateExpressionCommand(encodeBitSet(procs), expr);
		sendCommand(cmd);
	}

	public void debugGetType(BitList procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGetTypeCommand(encodeBitSet(procs), expr);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugListLocalVariables(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListLocalVariablesCommand(encodeBitSet(procs));
		sendCommand(cmd);
	}

	public void debugListArguments(BitList procs, int low, int high) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListArgumentsCommand(encodeBitSet(procs), low, high);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugListGlobalVariables(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListGlobalVariablesCommand(encodeBitSet(procs));
		sendCommand(cmd);
	}

	public void debugListInfoThreads(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListInfoThreadsCommand(encodeBitSet(procs));
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugSetThreadSelect(BitList procs, int threadNum) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetThreadSelectCommand(encodeBitSet(procs), threadNum);
		sendCommand(cmd);
	}
	
	public void setDataReadMemoryCommand(BitList procs, long offset, String address, String format, int wordSize, int rows, int cols, Character asChar) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDataReadMemoryCommand(encodeBitSet(procs), offset, address, format, wordSize, rows, cols, asChar);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void setDataWriteMemoryCommand(BitList procs, long offset, String address, String format, int wordSize, String value) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDataWriteMemoryCommand(encodeBitSet(procs), offset, address, format, wordSize, value);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugListSignals(BitList procs, String name) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListSignalsCommand(encodeBitSet(procs), name);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugSignalInfo(BitList procs, String arg) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSignalInfoCommand(encodeBitSet(procs), arg);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugCLIHandle(BitList procs, String arg) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugCLICommand(encodeBitSet(procs), arg);
		sendCommand(cmd);
		cmd.completed();
	}

	public void debugDataEvaluateExpression(BitList procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEvaluateExpressionCommand(encodeBitSet(procs), expr);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugGetPartialAIF(BitList procs, String name, String key, boolean listChildren, boolean express) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGetPartialAIFCommand(encodeBitSet(procs), name, key, listChildren, express);
		sendCommand(cmd);
		cmd.completed();
	}
	
	public void debugVariableDelete(BitList procs, String name) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugVariableDeleteCommand(encodeBitSet(procs), name);
		sendCommand(cmd);
		cmd.completed();
	}
}
