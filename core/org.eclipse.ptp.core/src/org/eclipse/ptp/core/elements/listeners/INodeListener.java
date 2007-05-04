package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.INodeChangedEvent;

public interface INodeListener {
	/**
	 * @param e
	 */
	public void handleEvent(INodeChangedEvent e);
}
