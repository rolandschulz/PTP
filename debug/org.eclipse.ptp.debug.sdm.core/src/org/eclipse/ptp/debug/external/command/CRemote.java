/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CRemote extends DebugCommand {
	public CRemote(String host, int port) {
		super("remote", new String[] {host, Integer.toString(port)});
	}
}
