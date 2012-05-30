package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IMachineChangeEvent;

@Deprecated
public interface IMachineListener {
	/**
	 * @param e
	 */
	public void handleEvent(IMachineChangeEvent e);
}
