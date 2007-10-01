package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;

public interface IResourceManagerListener {
	/**
	 * @param e
	 */
	public void handleEvent(IResourceManagerChangeEvent e);
	
	/**
	 * @param e
	 */
	public void handleEvent(IResourceManagerErrorEvent e);
}
