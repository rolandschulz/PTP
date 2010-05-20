package org.eclipse.ptp.debug.core.launch;

import java.util.Iterator;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

public class PLaunch extends Launch implements IPLaunch {
	private IPJob pJob;

	public PLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
	}

	public IPJob getPJob() {
		return pJob;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.launch.IPLaunch#setPJob(org.eclipse.ptp.core
	 * .elements.IPJob)
	 */
	public void setPJob(IPJob job) {
		pJob = job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.launch.IPLaunch#getDebugTarget(org.eclipse
	 * .ptp.debug.core.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDebugTarget getDebugTarget(TaskSet tasks) {
		for (final Iterator<?> i = getDebugTargets0().iterator(); i.hasNext();) {
			final IPDebugTarget debugTarget = (IPDebugTarget) i.next();
			if (debugTarget.getTasks().equals(tasks)) {
				return debugTarget;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#getDebugTarget(int)
	 */
	public IPDebugTarget getDebugTarget(int task_id) {
		for (final Iterator<?> i = getDebugTargets0().iterator(); i.hasNext();) {
			final IPDebugTarget debugTarget = (IPDebugTarget) i.next();
			if (debugTarget.getTasks().get(task_id)) {
				return debugTarget;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.Launch#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		if (pJob != null) {
			return pJob.getState() == JobAttributes.State.COMPLETED;
		}
		return super.isTerminated();
	}
}
