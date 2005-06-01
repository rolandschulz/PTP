/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.variable;

/**
 * @author donny
 *
 */
public class DebugVariable {
	String name = "";
	String value = "";
	String defaultValue = "";
	
	public DebugVariable(String nm) {
		name = nm;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String vl) {
		value = vl;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
}
