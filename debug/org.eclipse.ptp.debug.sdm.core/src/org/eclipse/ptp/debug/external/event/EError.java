package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EError extends DebugEvent {
	public EError(Hashtable s, int[] p) {
		super("inferiorExit", s, p);
	}
}
