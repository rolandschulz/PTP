/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.variable;

/**
 * @author donny
 *
 */
public class VMaxHistory extends DebugVariable {
	public VMaxHistory() {
		super("MAX_HISTORY");
		defaultValue = value = "20";
	}
}
