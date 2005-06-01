package org.eclipse.ptp.debug.external.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;

public class SessionObject implements ICDISessionObject {
	private Session fSession;

	public SessionObject (Session session) {
		fSession = session;
	}

	public ICDISession getSession() {
		return fSession;
	}
}
