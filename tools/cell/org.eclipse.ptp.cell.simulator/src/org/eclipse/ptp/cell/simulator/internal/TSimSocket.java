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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import org.eclipse.core.runtime.Assert;

class TSimSocket {
	Socket socket = null;
	BufferedReader reader = null;
	PrintWriter writer = null;
	
	public void connect(int port) throws UnknownHostException, IOException {
		connect(null, port);
	}
	
	public synchronized void connect(String host, int port) throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream());
	}
	
	public synchronized void disconnect() {
		if (socket == null) {
			return;
		}
		try {
			socket.close();
		} catch (IOException e) {
			// Ignore
		}
		socket = null;
		reader = null;
		writer = null;
	}
	
	public synchronized String[] query(String command) throws IOException, EOFException {
		Assert.isNotNull(writer);
		Assert.isNotNull(reader);
		
		writer.println(command);
		writer.flush();
		Vector result = new Vector();
		String line = reader.readLine();
		if (line == null) {
			throw new EOFException();
		}
		while (! line.equals("DONE")) { //$NON-NLS-1$
			result.add(line);
			line = reader.readLine();
			if (line == null) {
				throw new EOFException();
			}
		}
		
		String lines[] = new String[result.size()];
		lines = (String[]) result.toArray(lines);
		return lines;
	}
}
