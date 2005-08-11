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
	int[] eventProcesses;
	
	public DebugEvent(String eName, Hashtable eSources, int[] eProcesses) {
		eventName = eName;
		eventSources = eSources;
		eventProcesses = eProcesses;
	}
	
	public Hashtable getSources() {
		return eventSources;
	}
	
	public int[] getProcesses() {
		return eventProcesses;
	}
}
