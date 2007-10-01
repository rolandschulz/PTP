package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IProcessChangeEvent;

public interface IProcessListener {
	/**
	 * @param e
	 */
	public void handleEvent(IProcessChangeEvent e);
}
