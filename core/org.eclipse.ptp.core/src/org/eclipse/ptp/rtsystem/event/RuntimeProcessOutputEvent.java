package org.eclipse.ptp.rtsystem.event;

public class RuntimeProcessOutputEvent implements IRuntimeProcessOutputEvent {
	private String jobID;
	private String output;
	public RuntimeProcessOutputEvent(String jobID, String output) {
		this.jobID = jobID;
		this.output = output;
	}
	public String getJobID() {
		return jobID;
	}
	public String getOutput() {
		return output;
	}

}
