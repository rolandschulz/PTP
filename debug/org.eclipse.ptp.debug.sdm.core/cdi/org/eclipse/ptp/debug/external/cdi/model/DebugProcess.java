package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.external.model.MProcess;

import java.lang.Thread;

public class DebugProcess implements IPCDIDebugProcess {
	
	private MProcess pProcess;

	public DebugProcess(MProcess p) {
		pProcess = p;
	}
	
	public MProcess getMProcess() {
		return pProcess;
	}
	
	public String getName() {
		return pProcess.getName();
	}
}
