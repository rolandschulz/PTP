package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;

public class DebugProcess implements IPCDIDebugProcess {
	
	IPProcess pProcess;

	public DebugProcess(IPProcess p) {
		pProcess = p;
	}
	
	public IPProcess getPProcess() {
		return pProcess;
	}
	
	public IThread[] getThreads() throws DebugException {
		// Auto-generated method stub
		System.out.println("PDebugProcess.getThreads()");
		return null;
	}

	public boolean hasThreads() throws DebugException {
		// Auto-generated method stub
		System.out.println("PDebugProcess.hasThreads()");
		return false;
	}

	public String getName() {
		return pProcess.getElementName();
	}

}
