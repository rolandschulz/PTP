package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EInferiorExit extends DebugEvent {
	public EInferiorExit(Hashtable s, int[] p) {
		super("inferiorExit", s, p);
	}
}
