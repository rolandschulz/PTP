package org.eclipse.ptp.debug.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IPJob;

public interface IPLaunch extends ILaunch {
	public IPJob getPJob();
	public void setPJob(IPJob job);
	
	public IPSession getPSession();
	public void setPSession(IPSession session);
}
