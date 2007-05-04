package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IJobChangedEvent;

public interface IJobListener {
	/**
	 * @param e
	 */
	public void handleEvent(IJobChangedEvent e);
}
