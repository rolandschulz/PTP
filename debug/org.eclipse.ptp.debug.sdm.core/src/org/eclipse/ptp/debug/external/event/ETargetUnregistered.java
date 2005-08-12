package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class ETargetUnregistered extends DebugEvent {
	public ETargetUnregistered(Hashtable s, int[] p) {
		super("targetUnregistered", s, p);
	}
}
