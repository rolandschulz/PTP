package org.eclipse.ptp.debug.external.event;

import org.eclipse.ptp.debug.external.DebugSession;


public class EBreakpointHit extends DebugEvent {
	public EBreakpointHit(DebugSession s) {
		super("breakpointHit", s);
	}
}
