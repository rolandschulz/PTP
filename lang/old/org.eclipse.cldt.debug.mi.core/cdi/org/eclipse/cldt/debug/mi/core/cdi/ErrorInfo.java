/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cldt.debug.mi.core.cdi;

import org.eclipse.cldt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.cldt.debug.mi.core.event.MIErrorEvent;

/**
 */
public class ErrorInfo extends SessionObject implements ICDIErrorInfo {

	MIErrorEvent event;

	public ErrorInfo(Session session, MIErrorEvent e) {
		super(session);
		event = e;
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.ICDIErrorInfo#getMessage()
	 */
	public String getMessage() {
		return event.getMessage();
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.ICDIErrorInfo#getDetailMessage()
	 */
	public String getDetailMessage() {
		return event.getLogMessage();
	}

}
