package org.eclipse.ptp.rtsystem.event;

public interface IRuntimeErrorEvent extends IRuntimeEvent {
	public String getMessage();
	public int getCode();
}
