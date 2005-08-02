package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.model.IPDebugProcess;
import org.eclipse.ptp.debug.core.model.IPDebugProcessSet;

public class PDebugProcessSet extends PDebugElement implements IPDebugProcessSet {

	private IPCDIDebugProcessSet fPCDIProcessGroup;
	
	public PDebugProcessSet(PDebugTarget target, IPCDIDebugProcessSet cdiProcessGroup) {
		super(target);
		fPCDIProcessGroup = cdiProcessGroup;
	}
	
	public IPCDIDebugProcessSet getCDIProcessGroup() {
		return fPCDIProcessGroup;
	}

	public IPDebugProcess[] getProcesses() {
		IPCDIDebugProcess[] procs = fPCDIProcessGroup.getProcesses();
		IPDebugProcess[] result = new IPDebugProcess[procs.length];
		for (int i = 0; i < procs.length; i++) {
			result[i] = new PDebugProcess((PDebugTarget) getDebugTarget(), procs[i]);
		}
		return result;
	}

	public IPDebugProcess getProcess(int number) {
		IPCDIDebugProcess proc = fPCDIProcessGroup.getProcess(number);
		return new PDebugProcess((PDebugTarget) getDebugTarget(), proc);
	}

	public void addProcess(IPDebugProcess proc) {
		fPCDIProcessGroup.addProcess(((PDebugProcess) proc).getCDIProcess());
	}

	public void removeProcess(IPDebugProcess proc) {
		fPCDIProcessGroup.removeProcess(((PDebugProcess) proc).getCDIProcess());
	}
}
