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
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EInferiorCreated;
import org.eclipse.ptp.debug.external.model.MProcessSet;

public class Session implements IPCDISession, ICDISessionObject {
	EventManager eventManager;
	BreakpointManager breakpointManager;
	
	Properties props;
	SessionConfiguration configuration;
	
	DebugSession dSession;
	ILaunch dLaunch;
	IBinaryObject dBinObject;
	IDebugger debugger;
	
	Hashtable currentDebugTargetList;
	Hashtable currentProcessSetList;
	
	public Session(DebugSession dSess, ILaunch launch, IBinaryObject binObj) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		
		dSession = dSess;
		dLaunch = launch;
		dBinObject = binObj;
		debugger = dSession.getDebugger();
		
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		
		currentDebugTargetList = new Hashtable();
		currentProcessSetList = new Hashtable();
		
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
		registerTarget(0);
	}
	
	public DebugSession getDebugSession() {
		return dSession;
	}
	
	public IDebugger getDebugger() {
		return debugger;
	}
	
	public Process getProcess(int i) {
		return (Process) debugger.getProcess(i).getDebugInfo();
	}
	
	public IPCDIDebugProcessSet newProcessSet(String name, int[] procs) {
		if (currentProcessSetList.containsKey(name)) {
			return null;
		}

		MProcessSet procSet = debugger.defSet(name, procs);
		DebugProcessSet newSet = new DebugProcessSet(procSet);
		
		currentProcessSetList.put(newSet.getName(), newSet);
		
		return newSet;
	}
	
	public void delProcessSet(String name) {
		debugger.undefSet(name);
		currentProcessSetList.remove(name);
	}
	
	public IPCDIDebugProcessSet[] getProcessSets() {
		int size = currentProcessSetList.size();
		IPCDIDebugProcessSet[] pSets = new IPCDIDebugProcessSet[size];
		int index = 0;
		
	    Iterator it = currentProcessSetList.keySet().iterator();
	    while (it.hasNext()) {
	       String procSetName =  (String) it.next();
	       IPCDIDebugProcessSet procSet = (IPCDIDebugProcessSet) currentProcessSetList.get(procSetName);
	       pSets[index++] = procSet;
	    }
	    return pSets;
	}
	
	public IPCDIDebugProcessSet getProcessSet(String name) {
		return (IPCDIDebugProcessSet) currentProcessSetList.get(name);
	}

	public void registerTarget(int procNum) {
		if (isRegistered(procNum))
			return;
		
		Target target = new Target(this, debugger, procNum);

		currentDebugTargetList.put(Integer.toString(target.getTargetId()), target);
		
		Hashtable table = new Hashtable();
		table.put(new Integer(procNum), new int[] { 0 });
		debugger.fireEvent(new EInferiorCreated(table));
		
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
	
	public void registerTargets(int[] procNums) {
		for (int i = 0; i < procNums.length; i++) {
			registerTarget(procNums[i]);
		}
	}

	public void unregisterTarget(int procNum) {
		if (!isRegistered(procNum))
			return;
		
		// Remove DebugTarget & IProcess;
		PCDIDebugModel.removeDebugTarget(dLaunch, procNum);
		
		String targetId = Integer.toString(procNum);
		currentDebugTargetList.remove(targetId);
	}
	
	public void unregisterTargets(int[] targets) {
		for (int i = 0; i < targets.length; ++i) {
			unregisterTarget(targets[i]);
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

	public ICDITarget getTarget(int i) {
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

	public BreakpointManager getBreakpointManager() {
		return breakpointManager;
	}
	
	public ICDIEventManager getEventManager() {
		return eventManager;
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
		
		return debugger.getSessionProcess();
	}

	public ICDISession getSession() {
		return this;
	}
}
