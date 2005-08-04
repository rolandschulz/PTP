package org.eclipse.ptp.debug.external.event;



public class EInferiorCreated extends DebugEvent {
	public EInferiorCreated(int pId, int tId) {
		super("inferiorCreated", pId, tId);
	}
}
