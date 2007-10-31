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
package org.eclipse.ptp.debug.external.core.debugger;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.external.core.ExtFormat;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;

/**
 * @author clement
 *
 */
public class PDIDebugger extends ProxyDebugClient implements IPDIDebugger {
	private int bpid = 0;
	private ProxyNotifier proxyNotifier = new ProxyNotifier();

	private int newBreakpointId() {
		return bpid++;
	}
	public void commandRequest(BitList tasks, String command) throws PDIException {
		try {
			debugCLIHandle(tasks, command);
		}
		catch (IOException e) {
			throw new PDIException(null, "Error on sending generic command: " + e.getMessage());
		}
	}
	public void register(Observer observer) {
		proxyNotifier.addObserver(observer);
	}
	public boolean isConnected(IProgressMonitor monitor) throws PDIException {
		try {
			if (waitConnect(monitor)) {
				sessionHandleEvents();
				return true;
			}
			disconnection(null);
			return false;
		}
		catch (IOException e) {
			disconnection(null);
			throw new PDIException(null, "Error on connecting proxy: " + e.getMessage());
		}
	}
	public void disconnection(Observer observer) throws PDIException {
		stopDebugger();
	}
	public int getDebuggerPort(int timeout) throws PDIException {
		try {
			doConnect(timeout);
		}
		catch (IOException e) {
			throw new PDIException(null, "Error on getting proxy port number: " + e.getMessage());
		}
		return getSessionPort();
	}
	public void startDebugger(String app, String path, String dir, String[] args) throws PDIException {
		try {
			debugStartSession(app, path, dir, args);
		}
		catch (IOException e) {
			throw new PDIException(null, "Error on starting debugger: " + e.getMessage());
		}
	}
	public void stopDebugger() throws PDIException {
		try {
			doShutdown();
		}
		catch (IOException e) {
			throw new PDIException(null, "Error on stopping debugger: " + e.getMessage());
		}
		finally {
			proxyNotifier.deleteObservers();
			finalize();
		}
	}
	/*********************************************
	 * IPDISignalManagement
	 *********************************************/
	public void listSignals(BitList tasks, String name) throws PDIException {
		try {
			debugListSignals(tasks, name);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on listing signal: " + e.getMessage());
		}
	}
	public void retrieveSignalInfo(BitList tasks, String arg) throws PDIException {
		try {
			debugSignalInfo(tasks, arg);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on getting signal info: " + e.getMessage());
		}
	}
	/*********************************************
	 * IPDIMemoryManagement
	 *********************************************/
	private String getFormat(int wordFormat) {
		switch (wordFormat) {
		case ExtFormat.UNSIGNED :
			return "u";
		case ExtFormat.FLOAT :
			return "f";
		case ExtFormat.ADDRESS :
			return "a";
		case ExtFormat.INSTRUCTION :
			return "i";
		case ExtFormat.CHAR :
			return "c";
		case ExtFormat.STRING :
			return "s";
		case ExtFormat.DECIMAL :
			return "d";
		case ExtFormat.BINARY :
			return "t";
		case ExtFormat.OCTAL :
			return "o";
		case ExtFormat.HEXADECIMAL:
		default :
			return "x";
		}
	}	
	public void createDataReadMemory(BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) throws PDIException {
		try {
			setDataReadMemoryCommand(tasks, offset, address, getFormat(wordFormat), wordSize, rows, cols, asChar);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting data read memory: " + e.getMessage());
		}
	}
	public void createDataWriteMemory(BitList tasks, long offset, String address, int wordFormat, int wordSize, String value) throws PDIException {
		try {
			setDataWriteMemoryCommand(tasks, offset, address, getFormat(wordFormat), wordSize, value);			
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting data write memory: " + e.getMessage());
		}
	}
	/*********************************************
	 * IPDIVariableManagement
	 *********************************************/
	public void dataEvaluateExpression(BitList tasks, String expr) throws PDIException {
		try {
			debugDataEvaluateExpression(tasks, expr);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on evaluating data expression: " + e.getMessage());
		}
	}
	public void deleteVariable(BitList tasks, String var) throws PDIException {
		try {
			debugVariableDelete(tasks, var);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on deleting variable: " + e.getMessage());
		}
	}
	public void evaluateExpression(BitList tasks, String expr) throws PDIException {
		try {
			debugEvaluateExpression(tasks, expr);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on evaluating expression: " + e.getMessage());
		}
	}
	public void listArguments(BitList tasks, int low, int high) throws PDIException {
		try {
			debugListArguments(tasks, low, high);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on listing arguments: " + e.getMessage());
		}
	}
	public void listGlobalVariables(BitList tasks) throws PDIException {
		try {
			debugListGlobalVariables(tasks);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on listing global variables: " + e.getMessage());
		}
	}
	public void listLocalVariables(BitList tasks) throws PDIException {
		try {
			debugListLocalVariables(tasks);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on listing local variables: " + e.getMessage());
		}
	}
	public void retrieveAIF(BitList tasks, String expr) throws PDIException {
		try {
			debugEvaluateExpression(tasks, expr);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on getting aif: " + e.getMessage());
		}
	}
	public void retrievePartialAIF(BitList tasks, String expr, String key, boolean listChildren, boolean express) throws PDIException {
		try {
			debugGetPartialAIF(tasks, expr, key, listChildren, express);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on getting partial aif: " + e.getMessage());
		}
	}
	public void retrieveVariableType(BitList tasks, String var) throws PDIException {
		try {
			debugGetType(tasks, var);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on getting variable type: " + e.getMessage());
		}
	}
	/*********************************************
	 * IPDIThreadManagement
	 *********************************************/
	public void listInfoThreads(BitList tasks) throws PDIException {
		try {
			debugListInfoThreads(tasks);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on listing thread info: " + e.getMessage());
		}
	}
	public void retrieveStackInfoDepth(BitList tasks) throws PDIException {
		try {
			debugStackInfoDepth(tasks);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on getting stack info depth: " + e.getMessage());
		}
	}
	public void selectThread(BitList tasks, int tid) throws PDIException {
		try {
			debugSetThreadSelect(tasks, tid);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting thread id: " + e.getMessage());
		}
	}
	/*********************************************
	 * IPDIStackframeManagement
	 *********************************************/
	public void listStackFrames(BitList tasks, int low, int depth) throws PDIException {
		try {
			debugListStackframes(tasks, low, depth);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on listing stack frames: " + e.getMessage());
		}
	}
	public void setCurrentStackFrame(BitList tasks, int level) throws PDIException {
		try {
			debugSetCurrentStackframe(tasks, level);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting current stack frame level: " + e.getMessage());
		}
	}
	/*********************************************
	 * IPDIBreakpointManagement
	 *********************************************/
	public void deleteBreakpoint(BitList tasks, int bpid) throws PDIException {
		try {
			debugDeleteBreakpoint(tasks, bpid);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on deleting breakpoint: " + e.getMessage());
		}
	}
	public void setAddressBreakpoint(BitList tasks, IPDIAddressBreakpoint bpt) throws PDIException {
		throw new PDIException(tasks, "Not implement PDIDebugger - setAddressBreakpoint() yet");
	}
	public void setConditionBreakpoint(BitList tasks, int bpid, String condition) throws PDIException {
		try {
			debugConditionBreakpoint(tasks, bpid, condition);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting condition breakpoint: " + e.getMessage());
		}
	}
	public void setEnabledBreakpoint(BitList tasks, int bpid, boolean enabled) throws PDIException {
		try {
			if (enabled)
				debugEnableBreakpoint(tasks, bpid);
			else
				debugDisableBreakpoint(tasks, bpid);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting enabling breakpoint: " + e.getMessage());
		}
	}
	public void setExceptionpoint(BitList tasks, IPDIExceptionpoint bpt) throws PDIException {
		throw new PDIException(tasks, "Not implement PDIDebugger - setExceptionpoint() yet");
	}
	private String getFilename(String fullPath) {
		IPath path = new Path(fullPath);
		if (path.isEmpty())
			return "";
		return path.lastSegment();
	}
	public void setFunctionBreakpoint(BitList tasks, IPDIFunctionBreakpoint bpt) throws PDIException {
		try {
			IPDICondition condition = bpt.getCondition();
			int id = bpt.getBreakpointID();
			if (id == -1) {
				id = newBreakpointId();
				bpt.setBreakpointID(id);
			}
			//System.err.println("++Func Bpt - file: " + getFilename(bpt.getLocator().getFile()) + ", func: " + bpt.getLocator().getFunction());
			debugSetFuncBreakpoint(tasks, id, bpt.isTemporary(), bpt.isHardware(), getFilename(bpt.getLocator().getFile()), bpt.getLocator().getFunction(), (condition!=null?condition.getExpression():""), (condition!=null?condition.getIgnoreCount():0), 0);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting function breakpoint: " + e.getMessage());
		}
	}
	public void setLineBreakpoint(BitList tasks, IPDILineBreakpoint bpt) throws PDIException {
		try {
			IPDICondition condition = bpt.getCondition();
			int id = bpt.getBreakpointID();
			if (id == -1) {
				id = newBreakpointId();
				bpt.setBreakpointID(id);
			}
			//System.err.println("++Line Bpt - file: " + getFilename(bpt.getLocator().getFile()) + ", line: " + bpt.getLocator().getLineNumber());
			debugSetLineBreakpoint(tasks, id, bpt.isTemporary(), bpt.isHardware(), getFilename(bpt.getLocator().getFile()), bpt.getLocator().getLineNumber(), (condition!=null?condition.getExpression():""), (condition!=null?condition.getIgnoreCount():0), 0);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting line breakpoint: " + e.getMessage());
		}
	}
	public void setWatchpoint(BitList tasks, IPDIWatchpoint bpt) throws PDIException {
		try {
			String expression = bpt.getWatchExpression();
			boolean access = bpt.isReadType() && bpt.isWriteType(); 
			boolean read = ! bpt.isWriteType() && bpt.isReadType(); 
			IPDICondition condition = bpt.getCondition();
			int id = bpt.getBreakpointID();
			if (id == -1) {
				id = newBreakpointId();
				bpt.setBreakpointID(id);
			}
			debugSetWatchpoint(tasks, id, expression, access, read, (condition!=null?condition.getExpression():""), (condition!=null?condition.getIgnoreCount():0));
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on setting wacthpoint: " + e.getMessage());
		}
	}
	/*********************************************
	 * IPDIExecuteManagement
	 *********************************************/
	public void restart(BitList tasks) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - restart() yet");
	}
	public void start(BitList tasks) throws PDIException {
		resume(tasks, false);
	}
	public void resume(BitList tasks, boolean passSignal) throws PDIException {
		try {
			debugGo(tasks);
		}
		catch (IOException e) {
			throw new PDIException(tasks, "Error on resuming tasks: " + e.getMessage());
		}
	}
	public void resume(BitList tasks, IPDILocation location) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - resume(IPDILocation) yet");
	}
	public void resume(BitList tasks, IPDISignal signal) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - resume(IPDISignal) yet");
	}
	public void stepInto(BitList tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_INTO);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on stepping into: " + e.getMessage());
		}
	}
	public void stepIntoInstruction(BitList tasks, int count) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepIntoInstruction() yet");
	}
	public void stepOver(BitList tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_OVER);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on stepping over: " + e.getMessage());
		}
	}
	public void stepOverInstruction(BitList tasks, int count) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepOverInstruction() yet");
	}
	public void stepReturn(BitList tasks, IAIF aif) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepReturn(IAIF) yet");
	}
	public void stepReturn(BitList tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_FINISH);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on stepping return: " + e.getMessage());
		}
	}
	public void stepUntil(BitList tasks, IPDILocation location) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepUntil(IPDILocation) yet");
	}
	public void suspend(BitList tasks) throws PDIException {
		try {
			debugInterrupt(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on suspending tasks: " + e.getMessage());
		}
	}
	public void terminate(BitList tasks) throws PDIException {
		try {
			debugTerminate(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on terminating tasks: " + e.getMessage());
		}
	}
	/*********************************************
	 * IProxyDebugEventListener
	 *********************************************/
	public void handleEvent(IProxyExtendedEvent e) {
		if (e instanceof IProxyDebugEvent) {
			proxyNotifier.notify((IProxyDebugEvent)e);
		}
	}
	/*****************************************
	 * Proxy Notifier
	 *****************************************/
	class ProxyNotifier extends Observable {
		public void notify(IProxyDebugEvent event) {
			setChanged();
			notifyObservers(event);
		}
	}
	public int getErrorAction(int errorCode) {
        switch (errorCode) {
		case IParallelDebuggerConstants.DBGERR_NOBACKEND:
		case IParallelDebuggerConstants.DBGERR_DEBUGGER:
		case IParallelDebuggerConstants.DBGERR_NOFILEDIR:
		case IParallelDebuggerConstants.DBGERR_CHDIR:
        	return IPDIErrorInfo.DBG_FATAL;
        //case IParallelDebuggerConstants.DBGERR_INPROGRESS:
        case IParallelDebuggerConstants.DBGERR_UNKNOWN_TYPE:
		case IParallelDebuggerConstants.DBGERR_NOFILE:
		case IParallelDebuggerConstants.DBGERR_NOBP:
        	return IPDIErrorInfo.DBG_NORMAL;
		case IParallelDebuggerConstants.DBGERR_UNKNOWN_VARIABLE:
        	return IPDIErrorInfo.DBG_IGNORE;
		default:
        	return IPDIErrorInfo.DBG_WARNING;
        }
	}
}
