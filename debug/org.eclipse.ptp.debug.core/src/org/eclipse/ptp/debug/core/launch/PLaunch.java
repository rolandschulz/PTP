package org.eclipse.ptp.debug.core.launch;

import java.util.Iterator;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

public class PLaunch extends Launch implements IPLaunch {
	private IPJob pJob;

	public PLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
	}
	public IPJob getPJob() {
		return pJob;
	}
	public void setPJob(IPJob job) {
		pJob = job;
	}
	public IPDebugTarget getDebugTarget(BitList tasks) {
		for (Iterator<?> i=getDebugTargets0().iterator(); i.hasNext();) {
			IPDebugTarget debugTarget = (IPDebugTarget)i.next();
			if (debugTarget.getTasks().equals(tasks))
				return debugTarget;
		}
		return null;
	}
	public IPDebugTarget getDebugTarget(int task_id) {
		for (Iterator<?> i=getDebugTargets0().iterator(); i.hasNext();) {
			IPDebugTarget debugTarget = (IPDebugTarget)i.next();
			if (debugTarget.getTasks().get(task_id))
				return debugTarget;
		}
		return null;
	}
	public boolean isTerminated() {
		if (pJob != null)
			return pJob.isTerminated();
		return super.isTerminated();
	}
}
