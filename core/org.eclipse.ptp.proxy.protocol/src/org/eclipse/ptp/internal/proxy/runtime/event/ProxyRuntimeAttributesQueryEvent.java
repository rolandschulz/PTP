/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.proxy.runtime.event;

import org.eclipse.ptp.proxy.runtime.event.AbstractProxyRuntimeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeAttributesGetEvent;

public class ProxyRuntimeAttributesQueryEvent extends AbstractProxyRuntimeEvent
		implements IProxyRuntimeAttributesGetEvent {

	/**
	 * Create an event containing attributes queried in a QUERY_ATTRIBUTES
	 * command
	 * 
	 * @param transid
	 *            Transaction id for QUERY_ATTRIBUTES command
	 * @param args
	 *            Attribute values
	 */
	public ProxyRuntimeAttributesQueryEvent(int transid, String[] args) {
		super(ATTRS_QUERY, transid, args);
	}
}
