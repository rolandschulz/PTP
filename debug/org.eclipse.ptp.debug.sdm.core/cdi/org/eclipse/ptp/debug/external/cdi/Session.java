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
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EInferiorCreated;

public class Session implements ICDISession, ICDISessionObject {
	EventManager eventManager;
	BreakpointManager breakpointManager;
	VariableManager variableManager;
	
	Properties props;
	SessionConfiguration configuration;
	
	DebugSession dSession;
	ILaunch dLaunch;
	IBinaryObject dBinObject;
	IDebugger debugger;
	
	Hashtable currentDebugTargetList;
	
	public Session(DebugSession dSess, ILaunch launch, IBinaryObject binObj) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		
		dSession = dSess;
		dLaunch = launch;
		dBinObject = binObj;
		debugger = dSession.getDebugger();
		
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		variableManager = new VariableManager(this);
		
		currentDebugTargetList = new Hashtable();
		
		/* Initially we only create process/target 0 */
		//addTargets(new int[] { 0, 1 });
		addTarget(0);
		addTarget(1);
	}

	public void addTarget(int procNum) {
		Target target = new Target(this, dSession, procNum);
		
		debugger.addDebuggerObserver(eventManager);
		debugger.fireEvent(new EInferiorCreated(dSession));
		if (!currentDebugTargetList.containsKey(target.getTargetId())) {
			currentDebugTargetList.put(target.getTargetId(), target);
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
	
	public void addTargets(int[] procNums) {
		for (int i = 0; i < procNums.length; i++) {
			addTarget(procNums[i]);
		}
	}

	public void removeTargets(int[] targets) {
		for (int i = 0; i < targets.length; ++i) {
			String targetId = Integer.toString(targets[i]);
			Target target = (Target) currentDebugTargetList.remove(targetId);
			
			debugger.deleteDebuggerObserver(eventManager);
		}
	}

	public ICDITarget getTarget(int i) {
		//return processManager.getCDITarget(i);
		return (IPCDITarget) currentDebugTargetList.get(Integer.toString(i));
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
		System.out.println("Session.getBreakpointManager()");
		return breakpointManager;
	}
	
	public VariableManager getVariableManager() {
		System.out.println("Session.getVariableManager()");
		return variableManager;
	}

	public ICDIEventManager getEventManager() {
		System.out.println("Session.getEventManager()");
		return eventManager;
	}

	public ICDISessionConfiguration getConfiguration() {
		// Auto-generated method stub
		System.out.println("Session.getConfiguration()");
		return configuration;
	}

	public void terminate() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.terminate()");
		
	}

	public Process getSessionProcess() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.getSessionProcess()");
		
		return debugger.getSessionProcess();
	}

	public ICDISession getSession() {
		// Auto-generated method stub
		System.out.println("Session.getSession()");
		return this;
	}
}
