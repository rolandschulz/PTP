/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CStep extends DebugCommand {
	public CStep() {
		super("step");
	}
	
	public CStep(int count) {
		super("step", new String[] {Integer.toString(count)});
	}
}
