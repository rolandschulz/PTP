package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.external.model.MProcess;

public class DebugProcess implements IPCDIDebugProcess {
	
	private MProcess mProcess;

	public DebugProcess(MProcess p) {
		mProcess = p;
	}
	
	public MProcess getMProcess() {
		return mProcess;
	}
	
	public String getName() {
		return mProcess.getName();
	}
}
