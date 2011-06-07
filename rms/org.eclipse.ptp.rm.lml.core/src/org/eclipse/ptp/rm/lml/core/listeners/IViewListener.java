/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewDisposedEvent;

/**
 * This interface manages the handling of different events.
 */
public interface IViewListener extends IListener {

	/**
	 * Handles an ILguiAddedEvent.
	 * 
	 * @param e
	 *            an ILguiAddedEvent
	 */
	public void handleEvent(ILguiAddedEvent e);

	/**
	 * Handles an IlguiRemovedEvent.
	 * 
	 * @param e
	 *            an ILguiRemovedEvent
	 */
	public void handleEvent(ILguiRemovedEvent e);

	/**
	 * Handles an ILguiSelectedEvent.
	 * 
	 * @param e
	 *            an ILguiSelectedEvent
	 */
	public void handleEvent(ILguiSelectedEvent e);

	public void handleEvent(IViewAddedEvent e);

	public void handleEvent(IViewDisposedEvent e);
}
