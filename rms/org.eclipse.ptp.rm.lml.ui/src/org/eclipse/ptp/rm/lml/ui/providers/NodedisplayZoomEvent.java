package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;

/**
 * This implementation only saves the implicit name of
 * the node, which is shown in detail after this event
 * is send.
 * 
 */
public class NodedisplayZoomEvent implements INodedisplayZoomEvent {

	// Implicit name of new node
	private final String impname;

	/**
	 * Create a new event with implicit name of zoomed root-node.
	 * 
	 * @param impname
	 *            full name of shown node
	 */
	public NodedisplayZoomEvent(String impname) {
		this.impname = impname;
	}

	public String getNewNodeName() {
		return impname;
	}

}
