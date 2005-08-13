package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EInferiorCreated extends DebugEvent {
	public EInferiorCreated(BitSet s) {
		super("inferiorCreated", s);
	}
}
