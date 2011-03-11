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
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamMonitor;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

public class CommandJobStreamsProxy implements ICommandJobStreamsProxy {

	private ICommandJobStreamMonitor out;
	private ICommandJobStreamMonitor err;

	private boolean fClosed = false;
	private boolean fStarted = false;

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

	public void closeInputStream() throws IOException {
		throw new IOException(Messages.UnsupportedWriteException);
	}

	public IStreamMonitor getErrorStreamMonitor() {
		return err;
	}

	public IStreamMonitor getOutputStreamMonitor() {
		return out;
	}

	public void setErrMonitor(ICommandJobStreamMonitor err) {
		this.err = err;
	}

	public void setOutMonitor(ICommandJobStreamMonitor out) {
		this.out = out;
	}

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

	public void write(String input) throws IOException {
		throw new IOException(Messages.UnsupportedWriteException);
	}
}