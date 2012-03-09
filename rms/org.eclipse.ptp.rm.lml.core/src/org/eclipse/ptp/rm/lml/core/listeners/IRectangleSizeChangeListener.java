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

import org.eclipse.ptp.rm.lml.core.events.IRectangleSizeChangeEvent;

/**
 * Listens for changes of minimum rectangle sizes
 * within a nodedisplay.
 * 
 */
public interface IRectangleSizeChangeListener {

	/**
	 * This function is called for every event of rectangle size change events.
	 * 
	 * @param event
	 *            event containing information of the new rectangle size
	 */
	public void handleEvent(IRectangleSizeChangeEvent event);

}
