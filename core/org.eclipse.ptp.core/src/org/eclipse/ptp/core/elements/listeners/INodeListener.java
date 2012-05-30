package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.INodeChangeEvent;

@Deprecated
public interface INodeListener {
	/**
	 * @param e
	 */
	public void handleEvent(INodeChangeEvent e);
}
