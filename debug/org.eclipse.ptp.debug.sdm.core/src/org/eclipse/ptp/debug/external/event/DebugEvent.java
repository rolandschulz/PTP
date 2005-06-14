/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.event;

import org.eclipse.ptp.debug.external.DebugSession;

/**
 * @author donny
 *
 */
public class DebugEvent {
	String eventName = "";
	DebugSession dSession;
	
	public DebugEvent(String eName, DebugSession session) {
		eventName = eName;
		dSession = session;
	}
	
	public DebugSession getDebugSession() {
		return dSession;
	}
}
