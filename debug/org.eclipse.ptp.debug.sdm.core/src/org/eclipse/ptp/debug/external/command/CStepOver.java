/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CStepOver extends DebugCommand {
	public CStepOver() {
		super("stepOver");
	}
	
	public CStepOver(int count) {
		super("stepOver", new String[] {Integer.toString(count)});
	}
}
