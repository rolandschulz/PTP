/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;


/**
 * @author donny
 *
 */
public class DebugEvent {
	String eventName = "";
	BitSet eventSources;
	
	public DebugEvent(String eName, BitSet eSources) {
		eventName = eName;
		eventSources = eSources;
	}
	
	public BitSet getSources() {
		return eventSources;
	}
}
