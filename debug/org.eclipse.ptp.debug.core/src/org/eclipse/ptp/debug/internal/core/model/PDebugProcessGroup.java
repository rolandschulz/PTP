package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessGroup;
import org.eclipse.ptp.debug.core.model.IPDebugProcessGroup;

public class PDebugProcessGroup extends PDebugElement implements IPDebugProcessGroup {

	private IPCDIDebugProcessGroup fPCDIProcessGroup;
	
	public PDebugProcessGroup(PDebugTarget target, IPCDIDebugProcessGroup cdiProcessGroup) {
		super(target);
		fPCDIProcessGroup = cdiProcessGroup;
	}
	
	public IPCDIDebugProcessGroup getCDIProcessGroup() {
		return fPCDIProcessGroup;
	}
}
