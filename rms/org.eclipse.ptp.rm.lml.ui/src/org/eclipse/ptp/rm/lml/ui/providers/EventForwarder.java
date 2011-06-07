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
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus.Updatable;

/**
 * This class forwards events created in ObjectStatus to
 * the LMLManager. This is used in the nodedisplay to forward
 * mouse-events to tables.
 */
public class EventForwarder implements Updatable {

	private final LMLManager lmlmanager;// Default LMLManager attribute

	private ObjectType lastchanged;// Contains the ObjectType-instance, which was changed the last time
	private boolean wasmouseover, wasmousedown;// Corresponding state of lastchanged

	public EventForwarder() {
		lmlmanager = LMLManager.getInstance();
	}

	public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown) {
		// Translate ObjectStatus-Event to LML-Event
		if (j == null || j.getId() == null) {
			return;
		}

		final String oid = j.getId();

		if (mouseover) {
			lmlmanager.selectObject(oid);
		}

		if (mousedown) {
			lmlmanager.markObject(oid);
		}

		if (lastchanged == j) {
			if (wasmouseover && !mouseover) {
				lmlmanager.unselectObject(oid);
			}

			if (wasmouseover && mouseover && wasmousedown && !mousedown) {
				lmlmanager.unmarkObject(oid);
			}
		}
		// Save current state and object
		lastchanged = j;
		wasmouseover = mouseover;
		wasmousedown = mousedown;
	}

}
