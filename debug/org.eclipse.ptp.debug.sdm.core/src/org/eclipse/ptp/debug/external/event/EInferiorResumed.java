package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EInferiorResumed extends DebugEvent {
	public EInferiorResumed(Hashtable s, int[] p) {
		super("inferiorExit", s, p);
	}
}
