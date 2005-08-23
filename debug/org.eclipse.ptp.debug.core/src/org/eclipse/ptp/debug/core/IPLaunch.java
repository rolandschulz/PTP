package org.eclipse.ptp.debug.core;

import org.eclipse.ptp.core.IPJob;

public interface IPLaunch {
	public IPJob getPJob();
	public void setPJob(IPJob job);
	
	public IPSession getPSession();
	public void setPSession(IPSession session);
}
