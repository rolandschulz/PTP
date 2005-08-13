package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class ETargetUnregistered extends DebugEvent {
	public ETargetUnregistered(BitSet s) {
		super("targetUnregistered", s);
	}
}
