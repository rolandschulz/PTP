/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CFocus extends DebugCommand {
	public CFocus(String name) {
		super("focus", new String[] {name});
	}
}
