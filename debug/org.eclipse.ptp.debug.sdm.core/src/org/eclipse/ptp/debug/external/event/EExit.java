package org.eclipse.ptp.debug.external.event;



public class EExit extends DebugEvent {
	public EExit(int pId, int tId) {
		super("exit", pId, tId);
	}
}
