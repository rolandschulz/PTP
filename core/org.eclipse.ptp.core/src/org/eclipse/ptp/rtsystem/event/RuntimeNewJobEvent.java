package org.eclipse.ptp.rtsystem.event;

public class RuntimeNewJobEvent implements IRuntimeNewJobEvent {
	private String jobID;

	public String getJobID() {
		return jobID;
	}

	public RuntimeNewJobEvent(String jobID) {
		super();
		this.jobID = jobID;
	}
}
