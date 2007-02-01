package org.eclipse.ptp.rtsystem.event;

public class RuntimeJobStateChangedEvent implements IRuntimeJobStateChangedEvent {
	public String jobID;
	public String state;
	public RuntimeJobStateChangedEvent(String jobID, String state) {
		this.jobID = jobID;
		this.state = state;
	}
	public String getJobID() {
		return jobID;
	}
	public String getState() {
		return state;
	}
}
