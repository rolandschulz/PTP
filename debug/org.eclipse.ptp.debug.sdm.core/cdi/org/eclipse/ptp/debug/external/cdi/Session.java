package org.eclipse.ptp.debug.external.cdi;

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
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.model.Target;

public class Session implements ICDISession, ICDISessionObject {
	ProcessManager processManager;
	EventManager eventManager;
	BreakpointManager breakpointManager;
	VariableManager variableManager;
	
	Properties props;
	SessionConfiguration configuration;
	
	DebugSession dSession;
	ILaunch dLaunch;
	IBinaryObject dBinObject;
	
	public Session(DebugSession dSess, ILaunch launch, IBinaryObject binObj) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		
		dSession = dSess;
		dLaunch = launch;
		dBinObject = binObj;
		
		processManager = new ProcessManager(this);
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		variableManager = new VariableManager(this);
		
		/* Initially we only create process/target 0 */
		addTargets(new int[] { 0, 1 });
	}

	public void addTargets(int[] procNums) {
		Target[] targets = new Target[procNums.length];
		for (int i = 0; i < procNums.length; i++) {
			targets[i] = new Target(this, dSession, procNums[i]);
		}
		processManager.addTargets(targets);
		
		try {
			boolean stopInMain = dLaunch.getLaunchConfiguration().getAttribute( IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false );

			for (int i = 0; i < targets.length; i++) {
				Process process = targets[i].getProcess();
				IProcess iprocess = null;
				if (process != null) {
					iprocess = DebugPlugin.newProcess(dLaunch, process, "Launch Label " + targets[i].getTargetId());
				}

				PCDIDebugModel.newDebugTarget(dLaunch, null, targets[i], "Proc " + targets[i].getTargetId(), iprocess, dBinObject, true, false, stopInMain, true);
			}
		} catch (DebugException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		
	}

	public void removeTargets(int[] procNums) {
		processManager.removeTargets(procNums);
	}

	public ICDITarget getTarget(int i) {
		return processManager.getCDITarget(i);
	}
	
	public ICDITarget[] getTargets() {
		return processManager.getCDITargets();
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

	public Process getSessionProcess(ICDITarget target) {
		DebugSession dSession = ((Target)target).getDebugSession();
		return dSession.getDebugger().getSessionProcess();
	}

	public Process getSessionProcess() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.getSessionProcess()");
		
		ICDITarget[] targets = getTargets();
		if (targets != null && targets.length > 0) {
			DebugSession dS = ((Target) targets[0]).getDebugSession();
			return dS.getDebugger().getSessionProcess();
		}
		return null;
	}

	public ICDISession getSession() {
		// Auto-generated method stub
		System.out.println("Session.getSession()");
		return null;
	}
}
