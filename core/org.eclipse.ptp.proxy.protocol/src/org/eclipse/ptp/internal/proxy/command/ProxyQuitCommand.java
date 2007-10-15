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

package org.eclipse.ptp.internal.proxy.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;

public class ProxyQuitCommand extends AbstractProxyCommand {

	public int getCommandID() {
		return QUIT;
	}

	public String toString() {
		return "ProxyQuitCommand tid=" + getTransactionID();
	}

}
