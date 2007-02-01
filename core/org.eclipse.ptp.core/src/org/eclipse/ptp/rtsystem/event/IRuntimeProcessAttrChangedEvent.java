package org.eclipse.ptp.rtsystem.event;

import org.eclipse.ptp.core.util.BitList;

public interface IRuntimeProcessAttrChangedEvent extends IRuntimeEvent{
	public String getJobID();
	public BitList getProcesses();
	public String getState();
	public int[] getProcArray();
	public String[] getAttributeValues();
}
