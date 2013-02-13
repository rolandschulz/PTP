/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;

/**
 * CommandJob-specific extension of the stream proxy.
 * 
 * @see org.eclipse.debug.core.model.IStreamsProxy
 * @see org.eclipse.debug.core.model.IStreamsProxy2
 * 
 * @author arossi
 * 
 */
public interface ICommandJobStreamsProxy extends IStreamsProxy, IStreamsProxy2 {

	/**
	 * Manually close the proxy.
	 */
	public void close();

	/**
	 * @param err
	 *            monitor for error stream
	 */
	public void setErrMonitor(ICommandJobStreamMonitor err);

	/**
	 * 
	 * @param out
	 *            monitor for out stream
	 */
	public void setOutMonitor(ICommandJobStreamMonitor out);

	/**
	 * Attaches monitor to stream and begins reading.
	 */
	public void startMonitors();

}
