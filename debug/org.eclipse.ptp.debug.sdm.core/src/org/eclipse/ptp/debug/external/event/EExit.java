package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EExit extends DebugEvent {
	public EExit(Hashtable s) {
		super("exit", s);
	}
}
