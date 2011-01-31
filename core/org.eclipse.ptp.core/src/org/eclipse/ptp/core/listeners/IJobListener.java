package org.eclipse.ptp.core.listeners;

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
	 */
	public void handleEvent(IJobChangedEvent e);

}
