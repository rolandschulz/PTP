package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EBreakpointCreated extends DebugEvent {
	public EBreakpointCreated(Hashtable s, int[] p) {
		super("breakpointCreated", s, p);
	}
}
