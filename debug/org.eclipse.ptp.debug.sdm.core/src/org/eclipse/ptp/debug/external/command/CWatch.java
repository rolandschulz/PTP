/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CWatch extends DebugCommand {
	public CWatch(String var) {
		super("watch", new String[] {var});
	}
}
