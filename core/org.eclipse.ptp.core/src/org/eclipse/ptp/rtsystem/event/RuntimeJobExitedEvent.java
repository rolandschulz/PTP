package org.eclipse.ptp.rtsystem.event;

public class RuntimeJobExitedEvent implements IRuntimeJobExitedEvent {
	public String jobID;

	public RuntimeJobExitedEvent(String jobID) {
		this.jobID = jobID;
	}

	public String getJobID() {
		return jobID;
	}

}
