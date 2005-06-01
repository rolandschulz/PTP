/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class CViewSet extends DebugCommand {
	public CViewSet(String name) {
		super("viewSet", new String[] {name});
	}
}
