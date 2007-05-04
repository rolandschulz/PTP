package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IQueueChangedEvent;

public interface IQueueListener {
	/**
	 * @param e
	 */
	public void handleEvent(IQueueChangedEvent e);
}
