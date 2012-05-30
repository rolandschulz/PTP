/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rtsystem.events;

import org.eclipse.ptp.utils.core.RangeSet;

@Deprecated
public abstract class AbstractRuntimeRemoveEvent implements IRuntimeRemoveEvent {
	private final RangeSet elementIds;

	public AbstractRuntimeRemoveEvent() {
		this.elementIds = null;
	}

	public AbstractRuntimeRemoveEvent(RangeSet elementIds) {
		this.elementIds = elementIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeRemoveEvent#getElementIds()
	 */
	public RangeSet getElementIds() {
		return elementIds;
	}
}
