/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CSet extends DebugCommand {
	public CSet() {
		super("set");
	}
	
	public CSet(String name) {
		super("set", new String[] {name});
	}

	public CSet(String name, String val) {
		super("set", new String[] {name, val});
	}
}
