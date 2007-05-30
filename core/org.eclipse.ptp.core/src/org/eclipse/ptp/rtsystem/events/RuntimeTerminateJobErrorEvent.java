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


public class RuntimeTerminateJobErrorEvent 
	extends AbstractRuntimeErrorEvent
		implements IRuntimeTerminateJobErrorEvent {

	private String jobID;
	
	public RuntimeTerminateJobErrorEvent(int code, String message, String jobID) {
		super(code, message);
		this.jobID = jobID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent#getJobID()
	 */
	public String getJobID() {
		return jobID;
	}

}
