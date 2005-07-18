package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.model.IPDebugProcess;

public class PDebugProcess extends PDebugElement implements IPDebugProcess {
	
	private IPCDIDebugProcess fPCDIProcess;

	public PDebugProcess(PDebugTarget target, IPCDIDebugProcess cdiProcess) {
		super(target);
		fPCDIProcess = cdiProcess;
	}
	
	public IPCDIDebugProcess getCDIProcess() {
		return fPCDIProcess;
	}
	
	public int getProcessNumber() {
		return fPCDIProcess.getPProcessNumber();
	}
}
