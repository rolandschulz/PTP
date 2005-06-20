package org.eclipse.ptp.debug.external.event;

import org.eclipse.ptp.debug.external.DebugSession;


public class EBreakpointCreated extends DebugEvent {
	public EBreakpointCreated(DebugSession s) {
		super("breakpointCreated", s);
	}
}
