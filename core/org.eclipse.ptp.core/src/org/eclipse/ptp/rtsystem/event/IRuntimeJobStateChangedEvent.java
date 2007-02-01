package org.eclipse.ptp.rtsystem.event;

public interface IRuntimeJobStateChangedEvent extends IRuntimeEvent{
	public String getJobID();
	public String getState();
}
