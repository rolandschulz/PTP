package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EInferiorExit extends DebugEvent {
	public EInferiorExit(BitSet s) {
		super("inferiorExit", s);
	}
}
