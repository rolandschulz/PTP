/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CRun extends DebugCommand {
	public CRun() {
		super("run");
	}
	
	public CRun(String[] args) {
		super("run", args);
	}
}
