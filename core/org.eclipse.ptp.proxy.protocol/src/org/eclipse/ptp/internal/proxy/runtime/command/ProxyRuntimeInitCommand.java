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
package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.client.IProxyClient;
import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeInitCommand;

public class ProxyRuntimeInitCommand 
	extends AbstractProxyCommand implements IProxyRuntimeInitCommand {
	
	public final static String PROTOCOL_VERSION_ATTR = "version"; //$NON-NLS-1$
	public final static String BASE_ID_ATTR = "baseId"; //$NON-NLS-1$

	public ProxyRuntimeInitCommand(int baseId) {
		super(INIT);
		addArgument(PROTOCOL_VERSION_ATTR + "=" + IProxyClient.WIRE_PROTOCOL_VERSION); //$NON-NLS-1$
		addArgument(BASE_ID_ATTR + "=" + baseId); //$NON-NLS-1$
	}
	
	public ProxyRuntimeInitCommand(int transID, String[] args) {
		super(INIT, transID, args);
	}
}
