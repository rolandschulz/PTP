package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IQueueChangeEvent;

@Deprecated
public interface IQueueListener {
	/**
	 * @param e
	 */
	public void handleEvent(IQueueChangeEvent e);
}
