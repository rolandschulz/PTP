package org.eclipse.ptp.rtsystem.event;

import org.eclipse.ptp.core.util.BitList;

public class RuntimeProcessAttrChangedEvent implements IRuntimeProcessAttrChangedEvent {
	public String jobID;
	public BitList processes;
	public String state;
	public int[] procArray = new int[0];
	public String[] attributeValues = new String[0];
	public RuntimeProcessAttrChangedEvent(String jobID, BitList processes, String state, int[] procArray, String[] attributeValues) {
		this.jobID = jobID;
		this.processes = processes;
		this.state = state;
		this.procArray = procArray;
		this.attributeValues = attributeValues;
	}
	public String[] getAttributeValues() {
		return attributeValues;
	}
	public String getJobID() {
		return jobID;
	}
	public int[] getProcArray() {
		return procArray;
	}
	public BitList getProcesses() {
		return processes;
	}
	public String getState() {
		return state;
	}

}
