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
package org.eclipse.ptp.debug.internal.core.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import org.eclipse.debug.core.DebugPlugin;

/**
 * @author Clement chu
 * 
 */
public class PInputStreamMonitor {
	private OutputStream fStream;
	private Vector fQueue;
	private Thread fThread;
	private Object fLock;
	
	private boolean fClosed = false;
	
	public PInputStreamMonitor(OutputStream stream) {
		fStream = stream;
		fQueue = new Vector();
		fLock = new Object();
	}
	
	public void write(String text) {
		synchronized(fLock) {
			fQueue.add(text);
			fLock.notifyAll();
		}
	}
	public void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					write();
				}
			}, "Input Stream Monitor");
            fThread.setDaemon(true);
			fThread.start();
		}
	}
	
	public void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			thread.interrupt(); 
		}
	}
	protected void write() {
		while (fThread != null) {
			writeNext();
		}
		if (!fClosed) {
			try {
			    fStream.close();
			} catch (IOException e) {
				DebugPlugin.log(e);
			}
		}
	}
	protected void writeNext() {
		while (!fQueue.isEmpty() && !fClosed) {
			String text = (String)fQueue.firstElement();
			fQueue.removeElementAt(0);
			try {
				fStream.write(text.getBytes());
				fStream.flush();
			} catch (IOException e) {
				DebugPlugin.log(e);
			}
		}
		try {
			synchronized(fLock) {
				fLock.wait();
			}
		} catch (InterruptedException e) {
		}
	}
    public void closeInputStream() throws IOException {
        if (!fClosed) {
            fClosed = true;
            fStream.close();
        } else {
            throw new IOException();
        }
    }
}
