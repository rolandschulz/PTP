/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeFilterEventsCommand;

public class ProxyRuntimeFilterEventsCommand extends AbstractProxyCommand
		implements IProxyRuntimeFilterEventsCommand {

	public ProxyRuntimeFilterEventsCommand(int transID, String[] args) {
		super(FILTER_EVENTS, transID, args);
	}

	public ProxyRuntimeFilterEventsCommand(String[] args) {
		super(FILTER_EVENTS);
		addArguments(args);
	}
}
