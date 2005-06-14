package org.eclipse.ptp.debug.external.event;

import org.eclipse.ptp.debug.external.DebugSession;


public class EExit extends DebugEvent {
	public EExit(DebugSession s) {
		super("exit", s);
	}
}
