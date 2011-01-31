package org.eclipse.ptp.debug.core.launch;

import java.util.Iterator;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * @since 5.0
 */
public class PLaunch extends Launch implements IPLaunch {
	private String fJobId;
	private IResourceManagerControl fResourceManager;

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
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunch#getResourceManager()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerControl getResourceManager() {
		return fResourceManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.Launch#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		if (fResourceManager != null && fJobId != null) {
			return fResourceManager.getJobStatus(fJobId).getState() == JobAttributes.State.COMPLETED;
		}
		return super.isTerminated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.launch.IPLaunch#setPJob(org.eclipse.ptp.core
	 * .elements.IPJob)
	 */
	/**
	 * @since 5.0
	 */
	public void setJobId(String jobId) {
		fJobId = jobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.launch.IPLaunch#setResourceManager(org.eclipse
	 * .ptp.rmsystem.IResourceManagerControl)
	 */
	/**
	 * @since 5.0
	 */
	public void setResourceManager(IResourceManagerControl rm) {
		fResourceManager = rm;
	}
}
