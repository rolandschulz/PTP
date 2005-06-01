/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.actionpoint;

/**
 * @author donny
 *
 */
public class ABreakpoint extends DebugActionpoint {
	String location = "";
	int count = 0;
	String condition = "";
	
	public ABreakpoint(String loc) {
		super();
		location = loc;
	}
	
	public ABreakpoint(String loc, String cnd) {
		super();
		location = loc;
		condition = cnd;
	}
	
	public ABreakpoint(String loc, int cnt) {
		super();
		location = loc;
		count = cnt;
	}
}
