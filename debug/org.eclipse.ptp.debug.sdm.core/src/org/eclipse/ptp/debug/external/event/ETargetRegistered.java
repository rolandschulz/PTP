package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class ETargetRegistered extends DebugEvent {
	public ETargetRegistered(BitSet s) {
		super("targetRegistered", s);
	}
}
