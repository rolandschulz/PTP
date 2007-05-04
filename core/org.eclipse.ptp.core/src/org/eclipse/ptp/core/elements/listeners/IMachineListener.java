package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IMachineChangedEvent;

public interface IMachineListener {
	/**
	 * @param e
	 */
	public void handleEvent(IMachineChangedEvent e);
}
