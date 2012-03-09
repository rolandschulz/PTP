/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.core.events;

/**
 * This event is created for events of switching
 * root-nodes in nodedisplays. If a more detailed view
 * of one child-node is shown by zooming into the
 * shown nodedisplay, this event is created. It provides
 * information about the new node, which becomes root-
 * node now.
 * Which zoom-action was initiated can be accessed through
 * the getZoomType-function.
 */
public interface INodedisplayZoomEvent {

	/**
	 * Definition of different types of zooming
	 * 
	 */
	public static enum ZoomType {
		TREEZOOMIN, TREEZOOMOUT
	}

	/**
	 * @return name of the new root-node, which is shown now
	 *         null for the real root-node
	 */
	public String getNewNodeName();

	/**
	 * @return if this event was initiated by zooming in or zooming out
	 */
	public ZoomType getZoomType();

}
