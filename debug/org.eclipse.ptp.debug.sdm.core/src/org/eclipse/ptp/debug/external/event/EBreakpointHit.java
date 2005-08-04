package org.eclipse.ptp.debug.external.event;



public class EBreakpointHit extends DebugEvent {
	public EBreakpointHit(int pId, int tId) {
		super("breakpointHit", pId, tId);
	}
}
