package org.eclipse.ptp.debug.external.event;

import org.eclipse.ptp.debug.external.DebugSession;


public class EInferiorCreated extends DebugEvent {
	public EInferiorCreated(DebugSession s) {
		super("inferiorCreated", s);
	}
}
