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

import org.eclipse.ptp.rm.lml.core.events.IRectangleSizeChangeEvent;

/**
 * An instance of this events saves the current minimum size of
 * rectangles in the corresponding nodedisplay.
 */
public class RectangleSizeChangeEvent implements IRectangleSizeChangeEvent {

	/**
	 * Rectangle size in pixels
	 */
	private final int size;

	/**
	 * Create the event with the current minimum edge size of the rectangles.
	 * 
	 * @param size
	 *            edge size in pixels
	 */
	public RectangleSizeChangeEvent(int size) {
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.events.IRectangleSizeChangeEvent#getSize()
	 */
	public int getSize() {
		return size;
	}

}
