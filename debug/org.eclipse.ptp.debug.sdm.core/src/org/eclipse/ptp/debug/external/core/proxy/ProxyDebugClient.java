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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.external.core.proxy.command.IProxyDebugCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugBreakpointAfterCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugCLICommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugConditionBreakpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugDataReadMemoryCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugDataWriteMemoryCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugDeleteBreakpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugDisableBreakpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugEnableBreakpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugEvaluateExpressionCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugGetPartialAIFCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugGetTypeCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugGoCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugInterruptCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugListArgumentsCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugListGlobalVariablesCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugListInfoThreadsCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugListLocalVariablesCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugListSignalsCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugListStackframesCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugSetCurrentStackframeCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugSetFunctionBreakpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugSetLineBreakpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugSetThreadSelectCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugSetWatchpointCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugSignalInfoCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugStackInfoDepthCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugStartSessionCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugStepCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugTerminateCommand;
import org.eclipse.ptp.debug.external.core.proxy.command.ProxyDebugVariableDeleteCommand;

public class ProxyDebugClient extends AbstractProxyDebugClient {
	public static final int STEP_INTO = 0;
	public static final int STEP_OVER = 1;
	public static final int STEP_FINISH = 2;
	
	public void debugStartSession(String prog, String path, String dir, String[] args) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStartSessionCommand(this, prog, path, dir, args);
		cmd.send();
	}
	
	public void debugSetLineBreakpoint(BitList procs, int bpid, boolean isTemporary, boolean isHardware, String file, int line, String expression, int ignoreCount, int tid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetLineBreakpointCommand(this, procs, bpid, isTemporary, isHardware, file, line, expression, ignoreCount, tid);
		cmd.send();
	}
	
	public void debugSetFuncBreakpoint(BitList procs, int bpid, boolean isTemporary, boolean isHardware, String file, String func, String expression, int ignoreCount, int tid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetFunctionBreakpointCommand(this, procs, bpid, isTemporary, isHardware, file, func, expression, ignoreCount, tid);
		cmd.send();
	}
	
	public void debugSetWatchpoint(BitList procs, int bpid, String expression, boolean isAccess, boolean isRead, String condition, int ignoreCount) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetWatchpointCommand(this, procs, bpid, expression, isAccess, isRead, condition, ignoreCount);
		cmd.send();
	}
	
	public void debugDeleteBreakpoint(BitList procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDeleteBreakpointCommand(this, procs, bpid);
		cmd.send();
	}

	public void debugEnableBreakpoint(BitList procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEnableBreakpointCommand(this, procs, bpid);
		cmd.send();
	}

	public void debugDisableBreakpoint(BitList procs, int bpid) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDisableBreakpointCommand(this, procs, bpid);
		cmd.send();
	}

	public void debugConditionBreakpoint(BitList procs, int bpid, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugConditionBreakpointCommand(this, procs, bpid, expr);
		cmd.send();
	}

	public void debugBreakpointAfter(BitList procs, int bpid, int icount) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugBreakpointAfterCommand(this, procs, bpid, icount);
		cmd.send();
	}

	public void debugStackInfoDepth(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStackInfoDepthCommand(this, procs);
		cmd.send();
	}
	
	public void debugGo(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGoCommand(this, procs);
		cmd.send();
	}
	
	public void debugStep(BitList procs, int count, int type) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugStepCommand(this, procs, count, type);
		cmd.send();
	}
	
	public void debugTerminate(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugTerminateCommand(this, procs);
		cmd.send();
	}
	
	public void debugInterrupt(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugInterruptCommand(this, procs);
		cmd.send();
	}

	public void debugListStackframes(BitList procs, int low, int high) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListStackframesCommand(this, procs, low, high);
		cmd.send();
	}

	public void debugSetCurrentStackframe(BitList procs, int level) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetCurrentStackframeCommand(this, procs, level);
		cmd.send();
	}

	public void debugEvaluateExpression(BitList procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEvaluateExpressionCommand(this, procs, expr);
		cmd.send();
	}

	public void debugGetType(BitList procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGetTypeCommand(this, procs, expr);
		cmd.send();
	}

	public void debugListLocalVariables(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListLocalVariablesCommand(this, procs);
		cmd.send();
	}

	public void debugListArguments(BitList procs, int low, int high) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListArgumentsCommand(this, procs, low, high);
		cmd.send();
	}

	public void debugListGlobalVariables(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListGlobalVariablesCommand(this, procs);
		cmd.send();
	}

	public void debugListInfoThreads(BitList procs) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListInfoThreadsCommand(this, procs);
		cmd.send();
	}
	public void debugSetThreadSelect(BitList procs, int threadNum) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSetThreadSelectCommand(this, procs, threadNum);
		cmd.send();
	}
	
	public void setDataReadMemoryCommand(BitList procs, long offset, String address, String format, int wordSize, int rows, int cols, Character asChar) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDataReadMemoryCommand(this, procs, offset, address, format, wordSize, rows, cols, asChar);
		cmd.send();
	}
	
	public void setDataWriteMemoryCommand(BitList procs, long offset, String address, String format, int wordSize, String value) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugDataWriteMemoryCommand(this, procs, offset, address, format, wordSize, value);
		cmd.send();
	}

	public void debugListSignals(BitList procs, String name) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugListSignalsCommand(this, procs, name);
		cmd.send();
	}
	
	public void debugSignalInfo(BitList procs, String arg) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugSignalInfoCommand(this, procs, arg);
		cmd.send();
	}
	
	public void debugCLIHandle(BitList procs, String arg) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugCLICommand(this, procs, arg);
		cmd.send();
	}

	public void debugDataEvaluateExpression(BitList procs, String expr) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugEvaluateExpressionCommand(this, procs, expr);
		cmd.send();
	}
	
	public void debugGetPartialAIF(BitList procs, String name, String key, boolean listChildren, boolean express) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugGetPartialAIFCommand(this, procs, name, key, listChildren, express);
		cmd.send();
	}
	
	public void debugVariableDelete(BitList procs, String name) throws IOException {
		IProxyDebugCommand cmd = new ProxyDebugVariableDeleteCommand(this, procs, name);
		cmd.send();
	}
}
