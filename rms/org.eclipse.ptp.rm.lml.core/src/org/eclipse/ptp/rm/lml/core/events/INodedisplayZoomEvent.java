package org.eclipse.ptp.rm.lml.core.events;

/**
 * This is event is created for events of switching
 * root-nodes in nodedisplays. If a more detailed view
 * of one child-node is shown by zooming into the
 * shown nodedisplay, this event is created. It provides
 * information about the new node, which becomes root-
 * node now.
 */
public interface INodedisplayZoomEvent {

	/**
	 * @return name of the new root-node, which is shown now
	 */
	public String getNewNodeName();

}
