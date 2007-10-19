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

package org.eclipse.ptp.proxy.server;

import org.eclipse.ptp.proxy.command.IProxyCommandListener;

public interface IProxyServer {
	/**
	 * Add listener to receive proxy commands
	 * 
	 * @param listener listener to receive commands
	 */
	public void addListener(IProxyCommandListener listener);
	
	/**
	 * Remove listener from receiving proxy commands
	 * 
	 * @param listener listener to remove
	 */
	public void removeListener(IProxyCommandListener listener);
	
}
