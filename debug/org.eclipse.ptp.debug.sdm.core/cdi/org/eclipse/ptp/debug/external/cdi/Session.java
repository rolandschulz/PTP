package org.eclipse.ptp.debug.external.cdi;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.model.Target;

public class Session implements ICDISession, ICDISessionObject {
	
	public final static Target[] EMPTY_TARGETS = {};
	ProcessManager processManager;
	EventManager eventManager;
	BreakpointManager breakpointManager;
	VariableManager variableManager;
	RegisterManager registerManager;
	
	Properties props;
	SessionConfiguration configuration;
	
	public Session(DebugSession dSession) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		
		processManager = new ProcessManager(this);
		eventManager = new EventManager(this);
		breakpointManager = new BreakpointManager(this);
		variableManager = new VariableManager(this);
		registerManager = new RegisterManager(this);
		
		Target target = new Target(this, dSession);
		addTargets(new Target[] { target });
	}

	public void addTargets(Target[] targets) {
		processManager.addTargets(targets);
	}

	public void removeTargets(Target[] targets) {
		processManager.removeTargets(targets);
	}

	public Target getTarget(DebugSession miSession) {
		return processManager.getTarget(miSession);
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

	public RegisterManager getRegisterManager() {
		System.out.println("Session.getRegisterManager()");
		return registerManager;
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
