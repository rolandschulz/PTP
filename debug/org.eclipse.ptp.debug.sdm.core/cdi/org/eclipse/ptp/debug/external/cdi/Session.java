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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.utils.BitList;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.event.TargetRegisteredEvent;
import org.eclipse.ptp.debug.external.cdi.event.TargetUnregisteredEvent;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.model.Target;

public class Session implements IPCDISession, ICDISessionObject {
	EventManager eventManager;
	BreakpointManager breakpointManager;
	ExpressionManager expressionManager;
	VariableManager variableManager;
	ModelManager modelManager;
	
	Properties props;
	SessionConfiguration configuration;
	
	ILaunch dLaunch;
	IBinaryObject dBinObject;
	IDebugger debugger;
	IPJob dJob;
	
	Target targetZero; /* For compatibility we maintain the Target for Process 0 */
	Hashtable currentDebugTargetList;
	
	public Session(IPJob job, IDebugger iDebugger, ILaunch launch, IBinaryObject binObj) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		
		dLaunch = launch;
		dBinObject = binObj;
		debugger = iDebugger;
		debugger.setSession(this);
		dJob = job;
		
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		modelManager = new ModelManager(this);
		
		currentDebugTargetList = new Hashtable();
		
		debugger.addDebuggerObserver(eventManager);
		
		try {
			Process debugger = getSessionProcess();
			if (debugger != null) {
				IProcess debuggerProcess = DebugPlugin.newProcess(launch, debugger, "Debugger");
				launch.addProcess(debuggerProcess);
			}
		} catch (CDIException e) {
			
		}
		
		/* Initially we only create process/target 0 */
		registerTarget(0, true);
		targetZero = (Target) getTarget(0);
	}
	
	public IDebugger getDebugger() {
		return debugger;
	}
	
	public Process getProcess(int i) {
		return debugger.getProcess(i).getProcess();
	}
	
	public void registerTarget(int procNum, boolean sendEvent) {
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

			PCDIDebugModel.newDebugTarget(dLaunch, null, target, "Proc " + target.getTargetId(), iprocess, dBinObject, true, false, stopInMain, true);
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
		PCDIDebugModel.removeDebugTarget(dLaunch, procNum);
		
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

	public IPCDITarget getTarget() {
		return targetZero; /* For compatibility reason */
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
		// Auto-generated method stub
		System.out.println("Session.setAttribute()");
		props.setProperty(key, value);
	}

	public String getAttribute(String key) {
		// Auto-generated method stub
		System.out.println("Session.getAttribute()");
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

	public ModelManager getModelManager() {
		return modelManager;
	}

	public ExpressionManager getExpressionManager() {
		return expressionManager;
	}

	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}

	public void terminate() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.terminate()");
		
		debugger.deleteDebuggerObserver(eventManager);
	}

	public Process getSessionProcess() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.getSessionProcess()");
		
		return debugger.getDebuggerProcess();
	}

	public ICDISession getSession() {
		return this;
	}
}
