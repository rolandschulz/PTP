/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CLoad extends DebugCommand {
	public CLoad(String prg) {
		super("load", new String[] {prg});
	}
	
	public CLoad(String prg, int numProcs) {
		super("load", new String[] {prg, Integer.toString(numProcs)});
	}
}
