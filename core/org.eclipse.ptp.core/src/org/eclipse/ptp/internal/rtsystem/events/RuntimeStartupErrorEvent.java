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

import org.eclipse.ptp.rtsystem.events.AbstractRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeStartupErrorEvent;

public class RuntimeStartupErrorEvent
		extends AbstractRuntimeErrorEvent
		implements IRuntimeStartupErrorEvent {

	public RuntimeStartupErrorEvent(int code, String message) {
		super(code, message);
	}

	public RuntimeStartupErrorEvent(String message) {
		super(0, message);
	}
}
