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
package org.eclipse.ptp.internal.rm.lml.core.events;

import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;

/**
 * This implementation only saves the implicit name of
 * the node, which is shown in detail after this event
 * is send. The event also distinguishes between zooming in or
 * out. This means going more in detail or creating an overview.
 * Moreover it can be used to notify for rectangle-size
 * changes.
 * 
 */
public class NodedisplayZoomEvent implements INodedisplayZoomEvent {

	/**
	 * Implicit name of new node
	 */
	private final String impName;

	/**
	 * Type oft his event
	 */
	private ZoomType zoom;

	/**
	 * Create a new event with implicit name of zoomed root-node.
	 * 
	 * @param impName
	 *            full name of shown node
	 * @param zoomIn
	 *            if true this is a zoom-in-action, other it was zoomed out
	 */
	public NodedisplayZoomEvent(String impName, boolean zoomIn) {
		this.impName = impName;
		if (zoomIn) {
			zoom = ZoomType.TREEZOOMIN;
		}
		else {
			zoom = ZoomType.TREEZOOMOUT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent#getNewNodeName()
	 */
	public String getNewNodeName() {
		return impName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent#getZoomType()
	 */
	public ZoomType getZoomType() {
		return zoom;
	}

}
