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
package org.eclipse.ptp.debug.core.pdi.request;

/**
 * An event listener registers with the event request manager to receive
 * notification of event requests
 * 
 * @since 5.0
 * 
 */
public interface IPDIEventRequestListener {
	/**
	 * Notifies this listener that the given request has changed.
	 * 
	 * @param request
	 *            - the event request
	 */
	public void handleEventRequestChanged(IPDIEventRequest request);
}
