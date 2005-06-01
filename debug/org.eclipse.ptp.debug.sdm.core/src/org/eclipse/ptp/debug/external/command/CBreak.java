/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CBreak extends DebugCommand {
	public CBreak(String loc) {
		super("break", new String[] {loc});
	}
	
	public CBreak(String loc, int count) {
		super("break", new String[] {loc, Integer.toString(count)});
	}
	
	public CBreak(String loc, String cond) {
		super("break", new String[] {loc, cond});
	}
}
