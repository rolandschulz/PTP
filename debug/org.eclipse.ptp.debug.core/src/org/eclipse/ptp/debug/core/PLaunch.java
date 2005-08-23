package org.eclipse.ptp.debug.core;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.IPJob;

public class PLaunch extends Launch implements IPLaunch {
	private IPJob pJob;
	private IPSession pSession;
	
	public PLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
		// TODO Auto-generated constructor stub
	}

	public IPJob getPJob() {
		return pJob;
	}

	public void setPJob(IPJob job) {
		pJob = job;
	}

	public IPSession getPSession() {
		return pSession;
	}

	public void setPSession(IPSession session) {
		pSession = session;
	}

}
