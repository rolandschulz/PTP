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
package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;

/**
 * Listens for zooming events in nodedisplays.
 * If a specific sub-child in a nodedisplay is
 * set as root-element the handleEvent-function
 * is called.
 * 
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
