package org.eclipse.ptp.debug.external.event;

import java.util.Hashtable;

public class EDebuggerExit extends DebugEvent {
	public EDebuggerExit(Hashtable s, int[] p) {
		super("debuggerExit", s, p);
	}
}
