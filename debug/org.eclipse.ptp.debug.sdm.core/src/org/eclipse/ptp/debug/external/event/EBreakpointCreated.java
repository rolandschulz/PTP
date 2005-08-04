package org.eclipse.ptp.debug.external.event;



public class EBreakpointCreated extends DebugEvent {
	public EBreakpointCreated(int pId, int tId) {
		super("breakpointCreated", pId, tId);
	}
}
