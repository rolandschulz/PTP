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


package org.eclipse.ptp.internal.rtsystem.events;

import org.eclipse.ptp.rtsystem.events.AbstractRuntimeRemoveEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveJobEvent;
import org.eclipse.ptp.utils.core.RangeSet;

public class RuntimeRemoveJobEvent 
	extends AbstractRuntimeRemoveEvent 
		implements IRuntimeRemoveJobEvent {

	public RuntimeRemoveJobEvent(RangeSet ids) {
		super(ids);
	}

}
