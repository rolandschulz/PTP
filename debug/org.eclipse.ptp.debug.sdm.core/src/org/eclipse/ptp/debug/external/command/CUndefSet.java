/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CUndefSet extends DebugCommand {
	public CUndefSet(String name) {
		super("undefSet", new String[] {name});
	}
}
