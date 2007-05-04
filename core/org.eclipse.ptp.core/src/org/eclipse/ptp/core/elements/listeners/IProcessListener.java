package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IProcessChangedEvent;

public interface IProcessListener {
	/**
	 * @param e
	 */
	public void handleEvent(IProcessChangedEvent e);
}
