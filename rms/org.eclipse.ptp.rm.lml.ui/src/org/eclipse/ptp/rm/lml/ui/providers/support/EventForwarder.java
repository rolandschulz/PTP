/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers.support;

import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.model.ObjectStatus.Updatable;

/**
 * This class forwards events created in ObjectStatus to
 * the LMLManager. This is used in the nodedisplay to forward
 * mouse-events to tables.
 */
public class EventForwarder implements Updatable {

	/**
	 * Default LMLManager attribute
	 */
	private final LMLManager lmlManager;

	/**
	 * Contains the ObjectType-instance, which was changed the last time
	 */
	private ObjectType lastChanged;

	/**
	 * Corresponding state of lastchanged
	 */
	private boolean wasMouseOver, wasMouseDown;

	/**
	 * Initialise the forwarder by receiving a lmlManager-instance.
	 */
	public EventForwarder() {
		lmlManager = LMLManager.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rm.lml.core.model.ObjectStatus.Updatable#updateStatus(org.eclipse.ptp.rm.lml.core.elements
	 * .ObjectType, boolean, boolean)
	 */
	public void updateStatus(ObjectType object, boolean mouseOver, boolean mouseDown) {
		// Translate ObjectStatus-Event to LML-Event
		if (object == null || object.getId() == null) {
			return;
		}

		final String oid = object.getId();

		if (mouseOver) {
			lmlManager.selectObject(oid);
		}

		if (mouseDown) {
			lmlManager.markObject(oid);
		}

		if (lastChanged == object) {
			if (wasMouseOver && !mouseOver) {
				lmlManager.unselectObject(oid);
			}

			if (wasMouseOver && mouseOver && wasMouseDown && !mouseDown) {
				lmlManager.unmarkObject(oid);
			}
		}
		// Save current state and object
		lastChanged = object;
		wasMouseOver = mouseOver;
		wasMouseDown = mouseDown;
	}

}
