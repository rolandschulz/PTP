/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.smoa.core.sdk.attachments.FileStagingHandler;

/**
 * Staging handler working in cooperation with {@link SMOAFileStore} class.
 */
public class SMOAFileStagingHandler implements FileStagingHandler {

	public synchronized InputStream stageIn(String arg0) throws IOException {
		final TransferStream ts = new TransferStream();

		final SMOAFileStore fileStore = SMOAFileStore.fileStoresWaitingForStaging
				.get(arg0);
		SMOAFileStore.fileStoresWaitingForOutputStream.put(arg0, ts.os);

		synchronized (fileStore) {
			fileStore.notify();
		}

		return ts.is;
	}

	public synchronized void stageOut(String arg0, InputStream arg1)
			throws IOException {

		SMOAFileStore.fileStoresWaitingForInputStream.put(arg0, arg1);
	}
}

/**
 * Two connected Pipe(I/O)Streams. Needed when library has: Input method() and
 * user wants: method(Output)
 */
class TransferStream {
	PipedInputStream is = new PipedInputStream();
	PipedOutputStream os = new PipedOutputStream();
	{
		try {
			is.connect(os);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
