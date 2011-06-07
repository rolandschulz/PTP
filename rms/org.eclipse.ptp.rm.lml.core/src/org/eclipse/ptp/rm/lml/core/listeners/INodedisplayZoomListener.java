package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;

/**
 * Listens for zooming events in nodedisplays.
 * If a specific sub-child in a nodedisplay is
 * set as root-element the handleEvent-function
 * is called.
 */
public interface INodedisplayZoomListener {

	/**
	 * This function is called for every zoom-event.
	 * 
	 * @param event
	 *            event containing information of the zoomed node
	 */
	public void handleEvent(INodedisplayZoomEvent event);

}
