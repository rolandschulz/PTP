package org.eclipse.ptp.debug.external.core.cdi.model;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSetNamed;
import org.eclipse.ptp.debug.external.core.cdi.Session;
/**
 * @deprecated 
 */
public class DebugProcessSetNamed extends DebugProcessSet implements IPCDIDebugProcessSetNamed {
	public DebugProcessSetNamed(IPCDISession s, String name) {
		super((Session) s);
		setName = name;
	}

	public DebugProcessSetNamed(IPCDISession s, String name, BitList list) {
		super((Session) s, list);
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
