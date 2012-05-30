package org.eclipse.ptp.core.listeners;

import org.eclipse.ptp.core.events.IResourceManagerAddedEvent;
import org.eclipse.ptp.core.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.IResourceManagerRemovedEvent;

/**
 * @since 5.0
 */
@Deprecated
public interface IResourceManagerListener {

	/**
	 * Handle a resource manager added event
	 * 
	 * @param e
	 *            event
	 */
	public void handleEvent(IResourceManagerAddedEvent e);

	/**
	 * Handle a resource manager removed event
	 * 
	 * @param e
	 *            event
	 */
	public void handleEvent(IResourceManagerRemovedEvent e);

	/**
	 * Handle a resource manager status change event
	 * 
	 * @param e
	 *            event
	 */
	public void handleEvent(IResourceManagerChangedEvent e);

	/**
	 * Handle general resource manager error
	 * 
	 * @param e
	 *            event
	 */
	public void handleEvent(IResourceManagerErrorEvent e);
}
