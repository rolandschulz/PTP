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
package org.eclipse.ptp.rm.core.proxy;

/**
 * Options governing how remote proxy communication should work. The STDIO and PORT_FORWARDING options are mutually exclusive.
 * MANUAL_LAUNCH is only available if STDIO is *not* enabled.
 * 
 * No options - Use sockets for each proxy server that is started (runtime or debug).
 * 
 * STDIO - Multiplex all communication to a particular RM over a single connection using the proxy standard input and output. This
 * includes the runtime proxy, and any debug proxies started during the session.
 * 
 * PORT_FORWARDING- Use sockets for each proxy server, but attempt to use port forwarding (default) over the connection if
 * supported.
 * 
 * MANUAL_LAUNCH - Don't launch the proxy server automatically when the resource manager is started. Instead, the command that would
 * normally be executed is displayed on the console and in the error log. The user can then establish a manual connection and run
 * this command. This is mainly used for debugging the proxy server.
 * 
 * @since 4.0
 */

public interface IRemoteProxyOptions {
	/**
	 * No options selected
	 */
	public static final int NONE = 0x00;

	/**
	 * Multiplex all proxy communication over a single per-RM connection using stdio.
	 */
	public static final int STDIO = 0x01;

	/**
	 * Use port forwarding over RM connection, if supported
	 */
	public static final int PORT_FORWARDING = 0x02;

	/**
	 * Launch proxy manually
	 */
	public static final int MANUAL_LAUNCH = 0x04;
}
