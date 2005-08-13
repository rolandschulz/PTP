package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EError extends DebugEvent {
	public EError(BitSet s) {
		super("error", s);
	}
}
