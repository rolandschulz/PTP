package org.eclipse.ptp.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;

public class SessionConfiguration extends SessionObject implements ICDISessionConfiguration {
	public SessionConfiguration(Session session) {
		super(session);
	}

	public boolean terminateSessionOnExit() {
		return true;
	}
}