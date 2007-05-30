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

package org.eclipse.ptp.rtsystem.proxy.event;

import org.eclipse.ptp.core.elements.attributes.ErrorAttributes;

public class ProxyRuntimeStartupErrorEvent 
	extends AbstractProxyRuntimeEvent 
		implements IProxyRuntimeStartupErrorEvent {

	public ProxyRuntimeStartupErrorEvent(String message) {
		super(PROXY_RUNTIME_STARTUP_ERROR_EVENT, 0, new String[] {
				ErrorAttributes.getCodeAttributeDefinition().getId() + "=" + 0,
				ErrorAttributes.getMsgAttributeDefinition().getId() + "=" + message
		});
	}

	public ProxyRuntimeStartupErrorEvent(String[] attrs) {
		super(PROXY_RUNTIME_STARTUP_ERROR_EVENT, 0, attrs);
	}
}
