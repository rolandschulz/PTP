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

import org.eclipse.debug.core.model.IStreamMonitor;

/**
 * CommandJob-specific extension of the stream monitor.
 * 
 * @see org.eclipse.debug.core.model.IStreamMonitor
 * @author arossi
 * 
 */
public interface ICommandJobStreamMonitor extends IStreamMonitor {

	/**
	 * Append string to stream
	 * 
	 * @param text
	 *            text to append to the stream
	 */
	public void append(String text);

	/**
	 * Manually close the monitor/streams.
	 */
	public void close();

	/**
	 * Tune the monitor's buffer size
	 * 
	 * @param limit
	 *            in chars
	 * 
	 */
	public void setBufferLimit(int limit);

	/**
	 * Starts the reading from the stream.
	 */
	public void startMonitoring();
}
