/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CHistory extends DebugCommand {
	public CHistory() {
		super("history");
	}
	
	public CHistory(int numCmds) {
		super("history", new String[] {Integer.toString(numCmds)});
	}
}
