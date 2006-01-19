/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.external.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;

/**
 * @author Clement chu
 * @deprecated
 */
public class PStreamsProxy implements IStreamsProxy, IStreamsProxy2 {
	private POutputStreamMonitor fOutputMonitor;
	private POutputStreamMonitor fErrorMonitor;
	private PInputStreamMonitor fInputMonitor;
	private boolean fClosed= false;

	public PStreamsProxy(InputStream input, InputStream error, OutputStream output) {
		fOutputMonitor = new POutputStreamMonitor(input);
		fErrorMonitor = new POutputStreamMonitor(error);
		fInputMonitor = new PInputStreamMonitor(output);
		fOutputMonitor.startMonitoring();
		fErrorMonitor.startMonitoring();
		fInputMonitor.startMonitoring();
	}
	public void close() {
		if (!fClosed) {
			fClosed= true;
			fOutputMonitor.close();
			fErrorMonitor.close();
			fInputMonitor.close();
		}
	}
	public void kill() {
		fClosed= true;
		fOutputMonitor.kill();
		fErrorMonitor.kill();
		fInputMonitor.close();
	}
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}
	public IStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}
	public void write(String input) throws IOException {
		if (!fClosed) {
			fInputMonitor.write(input);
		} else {
			throw new IOException();
		}
	}
    public void closeInputStream() throws IOException {
        if (!fClosed) {
            fInputMonitor.closeInputStream();
        } else {
            throw new IOException();
        }
        
    }
}


