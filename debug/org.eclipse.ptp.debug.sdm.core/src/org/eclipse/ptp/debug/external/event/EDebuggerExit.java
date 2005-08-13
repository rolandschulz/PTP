package org.eclipse.ptp.debug.external.event;

import java.util.BitSet;

public class EDebuggerExit extends DebugEvent {
	public EDebuggerExit(BitSet s) {
		super("debuggerExit", s);
	}
}
