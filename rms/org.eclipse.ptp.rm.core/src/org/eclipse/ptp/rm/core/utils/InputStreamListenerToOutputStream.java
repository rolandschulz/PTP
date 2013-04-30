/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.core.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.ptp.rm.core.RMCorePlugin;

/**
 * An {@link IInputStreamListener} that forwards output to a piped stream.
 * 
 * @author Daniel Felix Ferber
 */
public class InputStreamListenerToOutputStream implements IInputStreamListener {

	boolean enabled = true;
	OutputStream outputStream;

	public InputStreamListenerToOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void newBytes(byte[] bytes, int length) {
		try {
			if (isEnabled()) {
				outputStream.write(bytes, 0, length);
			}
		} catch (IOException e) {
			disable();
			log(e);
		}
	}

	public void streamClosed() {
		try {
			disable();
			outputStream.close();
		} catch (IOException e) {
			log(e);
		}
	}

	public void streamError(Exception e) {
		disable();
		log(e);
	}

	protected void log(Exception e) {
		RMCorePlugin.log(e);
	}

	public synchronized void disable() {
		enabled = false;
	}

	public synchronized boolean isEnabled() {
		return enabled;
	}
}
