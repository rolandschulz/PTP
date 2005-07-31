/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;

import org.eclipse.ptp.debug.external.simulator.DebugSimulator;

/**
 * @author donny
 *
 */
public class DebugSession {
	IDebugger debugger;

	public DebugSession() {
		debugger = new DebugSimulator();
		debugger.initialize(this);
	}
	
	public IDebugger getDebugger() {
		return debugger;
	}

}
