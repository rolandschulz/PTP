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


public class RuntimeSubmitJobErrorEvent 
	extends AbstractRuntimeErrorEvent
		implements IRuntimeSubmitJobErrorEvent {

	private String jobSubID;
	
	public RuntimeSubmitJobErrorEvent(int code, String message, String jobSubID) {
		super(code, message);
		this.jobSubID = jobSubID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent#getJobSubID()
	 */
	public String getJobSubID() {
		return jobSubID;
	}

}
