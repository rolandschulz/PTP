/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.ptp.debug.external.cdi.event.ErrorEvent;

/**
 */
public class ErrorInfo extends SessionObject implements ICDIErrorInfo  {

	ErrorEvent event;

	public ErrorInfo(Session session, ErrorEvent e) {
		super(session);
		event = e;
	}

	public String getMessage() {
		// Auto-generated method stub
		System.out.println("ErrorInfo.getMessage()");
		return null;
	}

	public String getDetailMessage() {
		// Auto-generated method stub
		System.out.println("ErrorInfo.getDetailMessage()");
		return null;
	}

}
