package org.eclipse.ptp.debug.core;

import org.eclipse.ptp.debug.core.cdi.IPCDISession;

public class PSession implements IPSession {
	private IPCDISession pCDISession;
	
	public PSession(IPCDISession session) {
		pCDISession = session;
	}

	public IPCDISession getPCDISession() {
		return pCDISession;
	}
}
