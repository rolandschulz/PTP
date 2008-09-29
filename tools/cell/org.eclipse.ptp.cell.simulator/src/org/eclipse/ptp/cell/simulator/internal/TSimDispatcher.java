/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import org.eclipse.core.runtime.Assert;

public class TSimDispatcher extends Thread {
	TSimQueue queue = null;
	TSimSocket socket = null;
	
	public TSimDispatcher(TSimSocket socket, TSimQueue queue) {
		super("Simulator TCL dispatcher"); //$NON-NLS-1$
		this.socket = socket;
		this.queue = queue;
	}
	
	public TSimDispatcher(TSimSocket socket) {
		super();
		this.socket = socket;
		this.queue = new TSimQueue();
	}

	public void run() {
		Assert.isNotNull(queue);
		Assert.isNotNull(socket);
		
		while (! isInterrupted()) {
			try {
				ITSimRequest request = queue.popRequest();
				String query = request.getQuery();
				String response [] = socket.query(query);
				request.parseResponse(response);
			} catch (InterruptedException e) {
				return;
			} catch (InterruptedIOException e) {
				return;
			} catch (EOFException e) {
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public TSimQueue getQueue() {
		return queue;
	}

	public TSimSocket getSocket() {
		return socket;
	}
}
