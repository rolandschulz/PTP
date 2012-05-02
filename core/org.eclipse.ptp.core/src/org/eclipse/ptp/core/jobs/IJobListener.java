package org.eclipse.ptp.core.jobs;

/**
 * @since 6.0
 */
public interface IJobListener {
	/**
	 * Handle job added notification
	 * 
	 * @param e
	 *            event
	 * @since 6.0
	 */
	public void jobAdded(IJobStatus status);

	/**
	 * Handle job changed notification
	 * 
	 * @param e
	 *            event
	 */
	public void jobChanged(IJobStatus status);
}
