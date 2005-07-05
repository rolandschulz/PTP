package org.eclipse.ptp.debug.external.simulator;

public class SimVariable {

	String vName;
	String vType;
	
	public SimVariable(String name, String type) {
		vName = name;
		vType = type;
	}
	
	public String getName() {
		return vName;
	}
}
