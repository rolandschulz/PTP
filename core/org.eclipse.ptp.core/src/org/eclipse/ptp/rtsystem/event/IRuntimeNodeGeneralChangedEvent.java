package org.eclipse.ptp.rtsystem.event;

public interface IRuntimeNodeGeneralChangedEvent extends IRuntimeEvent{
	public String[] getKeys();
	public String[] getValues();
}
