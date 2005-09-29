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
import org.eclipse.ptp.debug.core.IPLaunch;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.IPCDIModelManager;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.utils.BitList;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.event.TargetRegisteredEvent;
import org.eclipse.ptp.debug.external.cdi.event.TargetUnregisteredEvent;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
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
	IDebugger debugger;
	IPJob dJob;
	
	Hashtable currentDebugTargetList;
	
	public Session(IDebugger iDebugger, IPLaunch launch, IBinaryObject binObj) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		
		dLaunch = launch;
		dBinObject = binObj;
		debugger = iDebugger;
		debugger.setSession(this);
		dJob = launch.getPJob();
		
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		modelManager = new ModelManager(this);
		
		/* Set the root process set */
		BitList root = new BitList();
		root.set(0, dJob.size());
		modelManager.newProcessSet("Root", root);
		
		currentDebugTargetList = new Hashtable();
		
		debugger.addDebuggerObserver(eventManager);
		
		try {
			Process debugger = getSessionProcess();
			if (debugger != null) {
				IProcess debuggerProcess = DebugPlugin.newProcess(dLaunch, debugger, "Debugger");
				dLaunch.addProcess(debuggerProcess);
			}
		} catch (CDIException e) {
		}
		
		/* Initially we only create process/target 0 */
		registerTarget(0, true, true);
	}
	
	public String getProjectName() {
		return dLaunch.getProjectName();
	}
	
	public IDebugger getDebugger() {
		return debugger;
	}
	
	public IPJob getJob() {
		return dJob;
	}
	
	public Process getProcess(int i) {
		return debugger.getProcess(i).getProcess();
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
			debugger.fireEvent(new TargetRegisteredEvent(this, new DebugProcessSet(this, procNum)));
			dJob.findProcessByTaskId(procNum).
				setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(true));
		}
		
		try {
			boolean stopInMain = dLaunch.getLaunchConfiguration().getAttribute( IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false );

			Process process = target.getProcess();
			IProcess iprocess = null;
			if (process != null) {
				iprocess = DebugPlugin.newProcess(dLaunch, process, "Launch Label " + target.getTargetId());
			}

			PCDIDebugModel.newDebugTarget(dLaunch, null, target, "Process " + target.getTargetId(), iprocess, dBinObject, true, false, stopInMain, resumeTarget);
		} catch (DebugException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public void registerTargets(int[] procNums, boolean sendEvent) {
		DebugProcessSet procSet = new DebugProcessSet(this);
		
		for (int i = 0; i < procNums.length; i++) {
			registerTarget(procNums[i], false);
			procSet.addProcess(procNums[i]);
			
			if (sendEvent)
				dJob.findProcessByTaskId(procNums[i]).
					setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(true));
		}
		
		if (sendEvent) {
			debugger.fireEvent(new TargetRegisteredEvent(this, procSet));
		}
	}

	public void unregisterTarget(int procNum, boolean sendEvent) {
		if (!isRegistered(procNum))
			return;
		
		// Remove DebugTarget & IProcess;
		PCDIDebugModel.removeDebugTarget(dLaunch, "Process " + procNum);
		
		String targetId = Integer.toString(procNum);
		currentDebugTargetList.remove(targetId);

		if (sendEvent) {
			debugger.fireEvent(new TargetUnregisteredEvent(this, new DebugProcessSet(this, procNum)));
			dJob.findProcessByTaskId(procNum).
				setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(false));
		}
	}
	
	public void unregisterTargets(int[] procNums, boolean sendEvent) {
		DebugProcessSet procSet = new DebugProcessSet(this);
		
		for (int i = 0; i < procNums.length; i++) {
			unregisterTarget(procNums[i], false);
			procSet.addProcess(procNums[i]);
			
			if (sendEvent)
				dJob.findProcessByTaskId(procNums[i]).
					setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(false));
		}
		
		if (sendEvent) {
			debugger.fireEvent(new TargetRegisteredEvent(this, procSet));
		}
	}
	
	public int[] getRegisteredTargetIds() {
		int size = currentDebugTargetList.size();
		int[] targetIds = new int[size];
		int index = 0;
		
	    Iterator it = currentDebugTargetList.keySet().iterator();
	    while (it.hasNext()) {
	       String targetId =  (String) it.next();
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
	       String targetId =  (String) it.next();
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
		System.out.println("Session.terminate()");
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
		System.out.println("Session.setWatchpoint()");
		return null;
	}

	public ICDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws CDIException {
		System.out.println("Session.setExceptionBreakpoint()");
		return null;
	}

	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		System.out.println("Session.getBreakpoints()");
		return null;
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		System.out.println("Session.deleteBreakpoints()");
	}

	public void deleteAllBreakpoints() throws CDIException {
		System.out.println("Session.deleteAllBreakpoints()");
	}
	
	public ICDILineBreakpoint setLineBreakpoint(int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		IPCDIDebugProcessSet newSet = new DebugProcessSet(this, 0);
		return setLineBreakpoint(newSet, type, location, condition, deferred);
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		IPCDIDebugProcessSet newSet = new DebugProcessSet(this, 0);
		return setFunctionBreakpoint(newSet, type, location, condition, deferred);
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		IPCDIDebugProcessSet newSet = new DebugProcessSet(this, 0);
		return setAddressBreakpoint(newSet, type, location, condition, deferred);
	}

	public ICDILineBreakpoint setLineBreakpoint(IPCDIDebugProcessSet bSet, int type, ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setLineBreakpoint(bSet, type, location, condition, deferred);
	}

	public ICDIFunctionBreakpoint setFunctionBreakpoint(IPCDIDebugProcessSet bSet, int type, ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setFunctionBreakpoint(bSet, type, location, condition, deferred);
	}

	public ICDIAddressBreakpoint setAddressBreakpoint(IPCDIDebugProcessSet bSet, int type, ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.setAddressBreakpoint(bSet, type, location, condition, deferred);
	}
	
	/* Location Management */
	
	public ICDILineLocation createLineLocation(String file, int line) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createLineLocation(file, line);
	}

	public ICDIFunctionLocation createFunctionLocation(String file, String function) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createFunctionLocation(file, function);
	}

	public ICDIAddressLocation createAddressLocation(BigInteger address) {
		BreakpointManager bMgr = ((Session)getSession()).getBreakpointManager();
		return bMgr.createAddressLocation(address);
	}

	/* Execution */
	
	public void stepOver(String setName) {
		stepOver(setName, 1);
	}

	public void stepOver(String setName, int count) {
		// Auto-generated method stub
		System.out.println("Session.stepOver()");
		IPCDIDebugProcessSet set = getModelManager().getProcessSet(setName);
		try {
			debugger.stepOver(set, count);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}

	public void stepInto(String setName) {
		stepInto(setName, 1);
	}

	public void stepInto(String setName, int count) {
		// Auto-generated method stub
		System.out.println("Session.stepInto()");
		IPCDIDebugProcessSet set = getModelManager().getProcessSet(setName);
		try {
			debugger.stepInto(set, count);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}

	public void stepFinish(String setName) {
		// Auto-generated method stub
		System.out.println("Session.stepFinish()");
		IPCDIDebugProcessSet set = getModelManager().getProcessSet(setName);
		try {
			debugger.stepFinish(set, 0);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	
	public void resume(String setName) {
		// Auto-generated method stub
		System.out.println("Session.resume()");
		IPCDIDebugProcessSet set = getModelManager().getProcessSet(setName);
		try {
			debugger.go(set);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
	
	public void suspend(String setName) {
		// Auto-generated method stub
		System.out.println("Session.suspend()");
		IPCDIDebugProcessSet set = getModelManager().getProcessSet(setName);
		try {
			debugger.halt(set);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}

	public void terminate(String setName) {
		// Auto-generated method stub
		System.out.println("Session.terminate()");
		IPCDIDebugProcessSet set = getModelManager().getProcessSet(setName);
		try {
			debugger.kill(set);
		} catch (PCDIException e) {
			e.printStackTrace();
		}
	}
}
