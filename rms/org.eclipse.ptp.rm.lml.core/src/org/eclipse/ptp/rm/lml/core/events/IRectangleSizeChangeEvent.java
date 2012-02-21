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
 * This event is used for nodedisplays, of which the minimum
 * size of painted rectangles is changed.
 * 
 */
public interface IRectangleSizeChangeEvent {

	/**
	 * @return new minimum size in pixels of the rectangles
	 */
	public int getSize();

}
