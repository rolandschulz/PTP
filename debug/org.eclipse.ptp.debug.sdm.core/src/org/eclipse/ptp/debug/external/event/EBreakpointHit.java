package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EBreakpointHit extends DebugEvent {
	public EBreakpointHit(Hashtable s) {
		super("breakpointHit", s);
	}
}
