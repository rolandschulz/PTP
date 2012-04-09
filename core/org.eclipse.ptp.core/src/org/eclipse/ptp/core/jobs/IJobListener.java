package org.eclipse.ptp.core.jobs;


/**
 * @since 6.0
 */
public interface IJobListener {
	/**
	 * Handle job notification
	 * 
	 * @param e
	 *            event
	 * @since 6.0
	 */
	public void handleEvent(IJobAddedEvent e);

	/**
	 * Handle job notification
	 * 
	 * @param e
	 *            event
	 */
	public void handleEvent(IJobChangedEvent e);
}
