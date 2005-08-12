package org.eclipse.ptp.debug.external.simulator;

public class SimVariable {

	String vName;
	String vType;
	String vValue;
	
	public SimVariable(String name, String type, String value) {
		vName = name;
		vType = type;
		vValue = value;
	}
	
	public String getName() {
		return vName;
	}
	
	public String getValue() {
		return vValue;
	}
}
