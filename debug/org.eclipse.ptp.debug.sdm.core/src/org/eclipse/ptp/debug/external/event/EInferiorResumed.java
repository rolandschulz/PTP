package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EInferiorResumed extends DebugEvent {
	public EInferiorResumed(BitSet s) {
		super("inferiorResumed", s);
	}
}
