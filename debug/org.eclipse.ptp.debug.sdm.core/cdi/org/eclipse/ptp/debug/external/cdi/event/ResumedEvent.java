/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.event.EInferiorResumed;

/**
 */
public class ResumedEvent extends AbstractEvent implements ICDIResumedEvent {
	
	public ResumedEvent(Session s, EInferiorResumed ev) {
		super(s, ev);
	}
	
	public int getType() {
		// Auto-generated method stub
		System.out.println("ResumedEvent.getType()");
		return 0;
	}
}
