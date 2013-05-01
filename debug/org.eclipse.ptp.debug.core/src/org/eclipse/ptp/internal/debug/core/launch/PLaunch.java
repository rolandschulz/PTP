package org.eclipse.ptp.internal.debug.core.launch;

import java.util.Iterator;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

/**
 * @since 5.0
 */
public class PLaunch extends Launch implements IPLaunch {
	private String fJobId;
	private IJobControl fJobControl;

	public PLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
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
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#getDebugTarget(org.eclipse .ptp.debug.core.TaskSet)
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
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#getJobControl()
	 */
	/**
	 * @since 6.0
	 */
	public IJobControl getJobControl() {
		return fJobControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#getJobId()
	 */
	/**
	 * @since 5.0
	 */
	public String getJobId() {
		return fJobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#setJobControl(org.eclipse.ptp.core.jobs.IJobControl)
	 */
	/**
	 * @since 6.0
	 */
	public void setJobControl(IJobControl control) {
		fJobControl = control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#setPJob(org.eclipse.ptp.core .elements.IPJob)
	 */
	/**
	 * @since 5.0
	 */
	public void setJobId(String jobId) {
		fJobId = jobId;
	}
}
