package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EBreakpointHit extends DebugEvent {
	public EBreakpointHit(BitSet s) {
		super("breakpointHit", s);
	}
}
