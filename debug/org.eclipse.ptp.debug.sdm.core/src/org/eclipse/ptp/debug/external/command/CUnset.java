/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CUnset extends DebugCommand {
	public CUnset(String name) {
		super("unset", new String[] {name});
	}
}
