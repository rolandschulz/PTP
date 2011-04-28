/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

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
	void close();

	/**
	 * @return handler for remote error file
	 */
	ICommandJobRemoteOutputHandler getRemoteErrorHandler();

	/**
	 * @return handler for remote output file
	 */
	ICommandJobRemoteOutputHandler getRemoteOutputHandler();

	/**
	 * If there are handlers, runs the check for their files and joins on those
	 * threads.
	 */
	void maybeWaitForHandlerFiles();

	/**
	 * @param err
	 *            monitor for error stream
	 */
	void setErrMonitor(ICommandJobStreamMonitor err);

	/**
	 * 
	 * @param out
	 *            monitor for out stream
	 */
	void setOutMonitor(ICommandJobStreamMonitor out);

	/**
	 * @param errHandler
	 *            for remote error file
	 */
	void setRemoteErrorHandler(ICommandJobRemoteOutputHandler errHandler);

	/**
	 * @param outHandler
	 *            for remote output file
	 */
	void setRemoteOutputHandler(ICommandJobRemoteOutputHandler outHandler);

	/**
	 * Attaches monitor to stream and begins reading.
	 */
	void startMonitors();

}
