package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EBreakpointCreated extends DebugEvent {
	public EBreakpointCreated(BitSet s) {
		super("breakpointCreated", s);
	}
}
