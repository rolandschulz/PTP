/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.actionpoint;

/**
 * @author donny
 *
 */
public class AWatchpoint extends DebugActionpoint {
	String var = "";
	
	public AWatchpoint(String debugVar) {
		super();
		var = debugVar;
	}
}
