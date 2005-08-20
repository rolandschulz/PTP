package org.eclipse.ptp.debug.external.cdi.model;

import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSetNamed;
import org.eclipse.ptp.debug.external.cdi.Session;

public class DebugProcessSetNamed extends DebugProcessSet implements IPCDIDebugProcessSetNamed {
	public DebugProcessSetNamed(IPCDISession s, String name) {
		super((Session) s);
		setName = name;
	}
	
	public DebugProcessSetNamed(IPCDISession s, String name, int[] procs) {
		super((Session) s, procs);
		setName = name;
	}
	
	public DebugProcessSetNamed(IPCDISession s, String name, int proc) {
		super((Session) s, proc);
		setName = name;
	}
}
