package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
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
}
