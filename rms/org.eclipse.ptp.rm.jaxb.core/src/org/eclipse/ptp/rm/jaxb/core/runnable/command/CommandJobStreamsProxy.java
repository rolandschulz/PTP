/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamMonitor;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

/**
 * Implementation of (@see org.eclipse.debug.core.model.IStreamsProxy, @see
 * org.eclipse.debug.core.model.IStreamsProxy2) adapted to resource manager
 * command jobs.<br>
 * <br>
 * 
 * Note that for batch submissions the stream monitors will be attached to a
 * tail -f stream rather than the stream from the actual running process.
 * 
 * @author arossi
 */
public class CommandJobStreamsProxy implements ICommandJobStreamsProxy {

	private ICommandJobStreamMonitor out;
	private ICommandJobStreamMonitor err;
	private ICommandJobRemoteOutputHandler outHandler;
	private ICommandJobRemoteOutputHandler errHandler;

	private boolean fClosed = false;
	private boolean fStarted = false;
	private boolean fFilesChecked = false;

	/**
	 * Closes both monitors.
	 */
	public synchronized void close() {
		if (!fClosed) {
			if (out != null) {
				out.close();
			}
			if (err != null) {
				err.close();
			}
			fClosed = true;
		}
	}

	/**
	 * At present we do not allow writing to the process. May be supported in
	 * the future.
	 */
	public void closeInputStream() throws IOException {
		JAXBCorePlugin.log(new IOException(Messages.UnsupportedWriteException));
	}

	/**
	 * @return monitor for error stream
	 */
	public IStreamMonitor getErrorStreamMonitor() {
		return err;
	}

	/**
	 * @return monitor for output stream
	 */
	public IStreamMonitor getOutputStreamMonitor() {
		return out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#getRemoteErrorHandler()
	 */
	public ICommandJobRemoteOutputHandler getRemoteErrorHandler() {
		return errHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#getRemoteOutputHandler()
	 */
	public ICommandJobRemoteOutputHandler getRemoteOutputHandler() {
		return outHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy#maybeWaitForHandlerFiles
	 * ()
	 */
	public void maybeWaitForHandlerFiles() {
		if (fFilesChecked) {
			return;
		}
		Thread tout = null;
		Thread terr = null;
		if (outHandler != null) {
			tout = outHandler.checkForReady(true);

		}
		if (errHandler != null) {
			terr = errHandler.checkForReady(true);
		}
		if (tout != null) {
			try {
				tout.join();
			} catch (InterruptedException ignored) {
			}
		}
		if (terr != null) {
			try {
				terr.join();
			} catch (InterruptedException ignored) {
			}
		}
		fFilesChecked = true;
	}

	/**
	 * @param err
	 *            monitor for error stream
	 */
	public void setErrMonitor(ICommandJobStreamMonitor err) {
		this.err = err;
	}

	/**
	 * @param out
	 *            monitor for output stream
	 */
	public void setOutMonitor(ICommandJobStreamMonitor out) {
		this.out = out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy#setRemoteErrorHandler
	 * (org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler)
	 */
	public void setRemoteErrorHandler(ICommandJobRemoteOutputHandler errHandler) {
		this.errHandler = errHandler;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy#setRemoteOutputHandler
	 * (org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler)
	 */
	public void setRemoteOutputHandler(ICommandJobRemoteOutputHandler outHandler) {
		this.outHandler = outHandler;
	}

	/**
	 * Called via the call to start the stream proxy for batch submissions
	 * (startProxy) or directly during the command for interactive.
	 */
	public synchronized void startMonitors() {
		if (!fClosed && !fStarted) {
			if (out != null) {
				out.startMonitoring();
			}
			if (err != null) {
				err.startMonitoring();
			}
			fStarted = true;
		}
	}

	/**
	 * At present we do not allow writing to the process. May be supported in
	 * the future.
	 */
	public void write(String input) throws IOException {
		JAXBCorePlugin.log(new IOException(Messages.UnsupportedWriteException));
	}
}