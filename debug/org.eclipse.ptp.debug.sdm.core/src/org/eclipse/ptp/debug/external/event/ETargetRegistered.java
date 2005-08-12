package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class ETargetRegistered extends DebugEvent {
	public ETargetRegistered(Hashtable s, int[] p) {
		super("targetRegistered", s, p);
	}
}
