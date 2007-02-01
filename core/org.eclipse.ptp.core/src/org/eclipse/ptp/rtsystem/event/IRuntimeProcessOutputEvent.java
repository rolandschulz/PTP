package org.eclipse.ptp.rtsystem.event;

public interface IRuntimeProcessOutputEvent extends IRuntimeEvent{
	public String getJobID();
	public String getOutput();
}
