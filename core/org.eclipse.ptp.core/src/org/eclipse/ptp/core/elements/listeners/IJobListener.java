package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IJobChangeEvent;

public interface IJobListener {
	/**
	 * @param e
	 */
	public void handleEvent(IJobChangeEvent e);
}
