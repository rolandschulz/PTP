/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;


/**
 * @author donny
 *
 */
public class DebugEvent {
	String eventName = "";
	Hashtable eventSources;
	
	public DebugEvent(String eName, Hashtable eSources) {
		eventName = eName;
		eventSources = eSources;
	}
	
	public Hashtable getSources() {
		return eventSources;
	}
}
