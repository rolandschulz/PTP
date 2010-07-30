/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */

package org.eclipse.ptp.internal.proxy.runtime.event;

import org.eclipse.ptp.proxy.runtime.event.AbstractProxyRuntimeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRMChangeEvent;

public class ProxyRuntimeRMChangeEvent extends AbstractProxyRuntimeEvent implements IProxyRuntimeRMChangeEvent {

	public ProxyRuntimeRMChangeEvent(int transID, String[] args) {
		super(RM_CHANGE, transID, args);
	}
}
