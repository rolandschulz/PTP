/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDIOutputEvent;

/**
 *
 */
public class OutputEvent extends AbstractEvent implements IPDIOutputEvent {
	private String fOutput;
	
	public OutputEvent(IPDISessionObject reason, TaskSet tasks, String output) {
		super(reason.getSession(), tasks);
		fOutput = output;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIOutputEvent#getOutput()
	 */
	public String getOutput() {
		return fOutput;
	}
}
