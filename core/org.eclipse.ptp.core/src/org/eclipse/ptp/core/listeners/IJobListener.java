package org.eclipse.ptp.core.listeners;

import org.eclipse.ptp.core.events.IJobAddedEvent;
import org.eclipse.ptp.core.events.IJobChangedEvent;

/**
 * @since 5.0
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
