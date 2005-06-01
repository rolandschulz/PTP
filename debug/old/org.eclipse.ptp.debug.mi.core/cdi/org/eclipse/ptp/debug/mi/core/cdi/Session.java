package org.eclipse.ptp.debug.mi.core.cdi;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.mi.core.cdi.model.Target;
import org.eclipse.ptp.debug.mi.core.gdb.MISession;

public class Session implements IPCDISession, ICDISessionObject {
	Properties props;
	SessionConfiguration configuration;
	Target[] targets;
	
	public Session(MISession[] miSessions) {
		props = new Properties();
		configuration = new SessionConfiguration(this);
		targets = new Target[] {new Target(this, miSessions)};
	}

	public ICDITarget[] getTargets() {
		return targets;
	}

	public void setAttribute(String key, String value) {
		// Auto-generated method stub
		System.out.println("Session.setAttribute()");
		
	}

	public String getAttribute(String key) {
		// Auto-generated method stub
		System.out.println("Session.getAttribute()");
		return null;
	}

	public ICDIEventManager getEventManager() {
		// Auto-generated method stub
		System.out.println("Session.getEventManager()");
		return null;
	}

	public ICDISessionConfiguration getConfiguration() {
		// Auto-generated method stub
		System.out.println("Session.getConfiguration()");
		return null;
	}

	public void terminate() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.terminate()");
		
	}

	public Process getSessionProcess() throws CDIException {
		// Auto-generated method stub
		System.out.println("Session.getSessionProcess()");
		if (targets != null && targets.length > 0) {
			MISession miSession = ((Target)targets[0]).getMISession();
			return miSession.getSessionProcess();
		}
		return null;
	}

	public ICDISession getSession() {
		// Auto-generated method stub
		System.out.println("Session.getSession()");
		return null;
	}
}
