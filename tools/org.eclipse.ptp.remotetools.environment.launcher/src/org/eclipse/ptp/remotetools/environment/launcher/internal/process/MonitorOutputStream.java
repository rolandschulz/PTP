/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.internal.process;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

class MonitorOutputStream extends OutputStream implements IStreamMonitor {
	private Set listeners= new HashSet(1);
	
	public void addListener(IStreamListener listener) {
		listeners.add(listener);
	}

	public String getContents() {
		return null;
	}

	public void removeListener(IStreamListener listener) {
		listeners.remove(listener);

	}

	public void write(int b) throws IOException {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			IStreamListener listener = (IStreamListener) iterator.next();
			byte bytes [] = new byte[1];
			bytes[0] = (byte)b;
			String text = new String(bytes);
			listener.streamAppended(text, this);
		}
	}
	
	public void write(byte[] b) throws IOException {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			IStreamListener listener = (IStreamListener) iterator.next();
			String text = new String(b);
			listener.streamAppended(text, this);
		}
	}

	public void write(byte[] b, int off, int len) throws IOException {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			IStreamListener listener = (IStreamListener) iterator.next();
			String text = new String(b, off, len);
			listener.streamAppended(text, this);
		}
	}
}
