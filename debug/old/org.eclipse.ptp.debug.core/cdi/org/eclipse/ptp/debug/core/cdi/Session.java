package org.eclipse.ptp.debug.core.cdi;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.ptp.debug.core.cdi.model.Target;

/**
 * @see org.eclipse.cdt.debug.core.cdi.ICDISession
 */
public class Session implements ICDISession, ICDISessionObject {

//	public final static Target[] EMPTY_TARGETS = {};
	Properties props;
//	ProcessManager processManager;
	EventManager eventManager;
/*	BreakpointManager breakpointManager;
	ExpressionManager expressionManager;
	VariableManager variableManager;
	RegisterManager registerManager;
	MemoryManager memoryManager;
	SharedLibraryManager sharedLibraryManager;
	SignalManager signalManager;
	SourceManager sourceManager;*/
	ICDISessionConfiguration configuration;
	ICDITarget[] fTargets;

	public Session(int nprocs) {
		props = new Properties();
		setConfiguration(new SessionConfiguration(this));

/*		processManager = new ProcessManager(this);
		breakpointManager = new BreakpointManager(this);*/
		eventManager = new EventManager(this);
/*		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		registerManager = new RegisterManager(this);
		memoryManager = new MemoryManager(this);
		signalManager = new SignalManager(this);
		sourceManager = new SourceManager(this);
		sharedLibraryManager = new SharedLibraryManager(this);*/
		
		fTargets = new Target[nprocs];
		for (int i = 0; i < nprocs; i++) {
			fTargets[i] = new Target(this);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getAttribute(String)
	 */
	public String getAttribute(String key) {
		return props.getProperty(key);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getEventManager()
	 */
	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getTargets()
	 */
	public ICDITarget[] getTargets() {
		return fTargets;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getConfiguration()
	 */
	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ICDISessionConfiguration conf) {
		configuration = conf;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#terminate(ICDITarget)
	 */
	public void terminate() throws CDIException {
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
	 */
	public Process getSessionProcess() throws CDIException {
		ICDITarget[] targets = getTargets();
		if (targets != null && targets.length > 0) {
			return getSessionProcess(targets[0]);
		}
		return null;
	}
	
	public Process getSessionProcess(ICDITarget target) {
		return ((Target)target).getProcess();
	}

}
