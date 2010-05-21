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
package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSuspendEventsCommand;

/**
 * Command class for SUSPEND_EVENTS command. This command is used in proxy flow
 * control to indicate the proxy must suspend sending event notifications to the
 * proxy
 * 
 * @author David Wootton
 * 
 */
public class ProxyRuntimeSuspendEventsCommand extends AbstractProxyCommand
		implements IProxyRuntimeSuspendEventsCommand {

	/**
	 * Create an instance of a SUSPEND_EVENTS command with no parameters
	 */
	public ProxyRuntimeSuspendEventsCommand() {
		super(SUSPEND_EVENTS);
	}

}
