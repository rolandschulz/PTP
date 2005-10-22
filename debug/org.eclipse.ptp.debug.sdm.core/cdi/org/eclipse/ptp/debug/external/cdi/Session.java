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
package org.eclipse.ptp.debug.external.cdi;

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPLaunch;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.IPCDIModelManager;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.IAbstractDebugger;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.event.TargetRegisteredEvent;
import org.eclipse.ptp.debug.external.cdi.event.TargetUnregisteredEvent;
import org.eclipse.ptp.debug.external.cdi.model.Target;

public class Session implements IPCDISession, ICDISessionObject, ICDIBreakpointManagement {
	EventManager eventManager;
	BreakpointManager breakpointManager;
	ExpressionManager expressionManager;
	VariableManager variableManager;
	IPCDIModelManager modelManager;
	Properties props;
	SessionConfiguration configuration;
	IPLaunch dLaunch;
	IBinaryObject dBinObject;
	IAbstractDebugger debugger;
	IPJob dJob;
	Hashtable currentDebugTargetList;

	public Session(IAbstractDebugger absDebugger, IPLaunch launch, IBinaryObject binObj) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		dLaunch = launch;
		dBinObject = binObj;
		this.debugger = absDebugger;
		this.debugger.setSession(this);
		dJob = launch.getPJob();
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		modelManager = new ModelManager(this);
		/* Set the root process set */
		BitList root = new BitList(dJob.size());
		root.set(0, dJob.size());
		modelManager.newProcessSet("Root", root);
		currentDebugTargetList = new Hashtable();
		this.debugger.addDebuggerObserver(eventManager);
		try {
			Process debuggerProc = getSessionProcess();
			if (debuggerProc != null) {
				IProcess iProcess = DebugPlugin.newProcess(dLaunch, debuggerProc, "Debugger");
				dLaunch.addProcess(iProcess);
			}
		} catch (CDIException e) {
		}
		try {
			boolean stopInMain = dLaunch.getLaunchConfiguration().getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
			if (stopInMain) {
				ICDIFunctionLocation location = createFunctionLocation("", "main");
				setInternalTemporaryBreakpoint(createBitList(), location);
			}
		} catch (CoreException e) {
		}
	}
	public String getProjectName() {
		return dLaunch.getProjectName();
	}
	public IAbstractDebugger getDebugger() {
		return debugger;
	}
	public IPJob getJob() {
		return dJob;
	}
	public Process getProcess(int i) {
		return debugger.getPseudoProcess(debugger.getProcess(i));
	}
	public void registerTarget(int procNum, boolean sendEvent) {
		registerTarget(procNum, sendEvent, false);
	}
	public void registerTarget(int procNum, boolean sendEvent, boolean resumeTarget) {
		if (isRegistered(procNum))
			return;
		Target target = new Target(this, debugger, procNum);
		currentDebugTargetList.put(Integer.toString(target.getTargetId()), target);
		if (sendEvent) {
			debugger.fireEvent(new TargetRegisteredEvent(this, createBitList(procNum)));
			dJob.findProcessByTaskId(procNum).setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(true));
		}
		try {
			Process process = target.getProcess();
			IProcess iprocess = null;
			if (process != null) {
				iprocess = DebugPlugin.newProcess(dLaunch, process, "Launch Label " + target.getTargetId());
			}
			PCDIDebugModel.newDebugTarget(dLaunch, null, target, "Process " + target.getTargetId(), iprocess, dBinObject, true, false, resumeTarget);
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}
	public void registerTargets(int[] procNums, boolean sendEvent) {
		BitList tasks = new BitList(getTotalProcesses());
		for (int i = 0; i < procNums.length; i++) {
			registerTarget(procNums[i], false);
			tasks.set(procNums[i]);
			if (sendEvent)
				dJob.findProcessByTaskId(procNums[i]).setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(true));
		}
		if (sendEvent) {
			debugger.fireEvent(new TargetRegisteredEvent(this, tasks));
		}
	}
	public void unregisterTarget(int procNum, boolean sendEvent) {
		if (!isRegistered(procNum))
			return;
		// Remove DebugTarget & IProcess;
		PCDIDebugModel.removeDebugTarget(dLaunch, "Process " + procNum);
		String targetId = Integer.toString(procNum);
		currentDebugTargetList.remove(targetId);
		debugger.removePseudoProcess(debugger.getProcess(procNum));
		if (sendEvent) {
			debugger.fireEvent(new TargetUnregisteredEvent(this, createBitList(procNum)));
			dJob.findProcessByTaskId(procNum).setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(false));
		}
	}
	public void unregisterTargets(int[] procNums, boolean sendEvent) {
		BitList tasks = new BitList(getTotalProcesses());
		for (int i = 0; i < procNums.length; i++) {
			unregisterTarget(procNums[i], false);
			tasks.set(procNums[i]);
			if (sendEvent)
				dJob.findProcessByTaskId(procNums[i]).setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(false));
		}
		if (sendEvent) {
			debugger.fireEvent(new TargetRegisteredEvent(this, tasks));
		}
	}
	public int[] getRegisteredTargetIds() {
		int size = currentDebugTargetList.size();
		int[] targetIds = new int[size];
		int index = 0;
		Iterator it = currentDebugTargetList.keySet().iterator();
		while (it.hasNext()) {
			String targetId = (String) it.next();
			targetIds[index++] = Integer.parseInt(targetId);
		}
		return targetIds;
	}
	public boolean isRegistered(int i) {
		return currentDebugTargetList.containsKey(Integer.toString(i));
	}
	public IPCDITarget getTarget(int i) {
		if (isRegistered(i))
			return (IPCDITarget) currentDebugTargetList.get(Integer.toString(i));
		else {
			return null;
		}
	}
	public ICDITarget[] getTargets() {
		int size = currentDebugTargetList.size();
		IPCDITarget[] targets = new IPCDITarget[size];
		int index = 0;
		Iterator it = currentDebugTargetList.keySet().iterator();
		while (it.hasNext()) {
			String targetId = (String) it.next();
			IPCDITarget target = (IPCDITarget) currentDebugTargetList.get(targetId);
			targets[index++] = target;
		}
		return targets;
	}
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}
	public String getAttribute(String key) {
		return props.getProperty(key);
	}
	public VariableManager getVariableManager() {
		return variableManager;
	}
	public BreakpointManager getBreakpointManager() {
		return breakpointManager;
	}
	public ICDIEventManager getEventManager() {
		return eventManager;
	}
	public IPCDIModelManager getModelManager() {
		return modelManager;
	}
	public ExpressionManager getExpressionManager() {
		return expressionManager;
	}
	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}
	public void terminate() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		debugger.deleteDebuggerObserver(eventManager);
	}
	public Process getSessionProcess() throws CDIException {
		return debugger.getDebuggerProcess();
	}
	public ICDISession getSession() {
		return this;
	}
	/* Breakpoint Management */
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}
	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}
	public void deleteAllBreakpoints() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}
	public BitList createBitList() {
		BitList tasks = new BitList(getTotalProcesses());
		tasks.set(0, getTotalProcesses());
		return tasks;
	}
	public BitList createBitList(int index) {
		BitList tasks = new BitList(getTotalProcesses());
		tasks.set(index);
		return tasks;
	}
	// TODO who will call this method
	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		return setLineBreakpoint(createBitList(), type, location, condition, deferred);
	}
	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		return setFunctionBreakpoint(createBitList(), type, location, condition, deferred);
	}
	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		return setAddressBreakpoint(createBitList(), type, location, condition, deferred);
	}
	public ICDILineBreakpoint setLineBreakpoint(BitList tasks, int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		BreakpointManager bMgr = ((Session) getSession()).getBreakpointManager();
		return bMgr.setLineBreakpoint(this, tasks, type, location, condition, deferred);
	}
	public ICDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		BreakpointManager bMgr = ((Session) getSession()).getBreakpointManager();
		return bMgr.setFunctionBreakpoint(this, tasks, type, location, condition, deferred);
	}
	public ICDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		BreakpointManager bMgr = ((Session) getSession()).getBreakpointManager();
		return bMgr.setAddressBreakpoint(this, tasks, type, location, condition, deferred);
	}
	public void setInternalTemporaryBreakpoint(BitList tasks, ICDILocation location) throws DebugException {
		try {
			if (location instanceof ICDIFunctionLocation) {
				setFunctionBreakpoint(tasks, ICDIBreakpoint.TEMPORARY, (ICDIFunctionLocation) location, null, false);
			} else if (location instanceof ICDILineLocation) {
				setLineBreakpoint(tasks, ICDIBreakpoint.TEMPORARY, (ICDILineLocation) location, null, false);
			} else if (location instanceof ICDIAddressLocation) {
				setAddressBreakpoint(tasks, ICDIBreakpoint.TEMPORARY, (ICDIAddressLocation) location, null, false);
			}
		} catch (CDIException e) {
		}
	}
	/* Location Management */
	public ICDILineLocation createLineLocation(String file, int line) {
		BreakpointManager bMgr = ((Session) getSession()).getBreakpointManager();
		return bMgr.createLineLocation(file, line);
	}
	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		BreakpointManager bMgr = ((Session) getSession()).getBreakpointManager();
		return bMgr.createFunctionLocation(file, function);
	}
	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		BreakpointManager bMgr = ((Session) getSession()).getBreakpointManager();
		return bMgr.createAddressLocation(address);
	}
	/* Execution */
	public void stepOver(BitList tasks) {
		stepOver(tasks, 1);
	}
	public void stepOver(BitList tasks, int count) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		try {
			getDebugger().stepOverAction(tasks, count);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	public void stepInto(BitList tasks) {
		stepInto(tasks, 1);
	}
	public void stepInto(BitList tasks, int count) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		try {
			getDebugger().stepIntoAction(tasks, count);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	public void stepFinish(BitList tasks) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		try {
			getDebugger().stepFinishAction(tasks, 0);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	public void resume(BitList tasks) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		try {
			getDebugger().goAction(tasks);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	public void suspend(BitList tasks) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		try {
			getDebugger().haltAction(tasks);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	public void terminate(BitList tasks) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		try {
			getDebugger().killAction(tasks);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	public int getTotalProcesses() {
		return dJob.size();
	}
}
