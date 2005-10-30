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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;

/**
 * @author Clement chu
 * 
 */
public class POutputStreamMonitor implements IFlushableStreamMonitor {
	private InputStream fStream;
	private List fListeners = new ArrayList(1);
	private boolean fBuffered = true;
	private StringBuffer fContents;
	private Thread fThread;

	private static final int BUFFER_SIZE = 8192;

	private boolean fKilled= false;
    private long lastSleep;
    
	public POutputStreamMonitor(InputStream stream) {
        fStream = new BufferedInputStream(stream, BUFFER_SIZE);
		fContents= new StringBuffer();
	}

	public synchronized void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}
	protected void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			try {
				thread.join();
			} catch (InterruptedException ie) {
			}
			fListeners.clear();
		}
	}

	private void fireStreamAppended(String text) {
		getNotifier().notifyAppend(text);
	}
	public synchronized String getContents() {
		return fContents.toString();
	}
	private void read() {
        lastSleep = System.currentTimeMillis();
        long currentTime = lastSleep;
		byte[] bytes= new byte[BUFFER_SIZE];
		int read = 0;
		while (read >= 0) {
			try {
				if (fKilled) {
					break;
				}
				read= fStream.read(bytes);
				if (read > 0) {
					String text= new String(bytes, 0, read);
					synchronized (this) {
						if (isBuffered()) {
							fContents.append(text);
						}
						fireStreamAppended(text);
					}
				}
			} catch (IOException ioe) {
				DebugPlugin.log(ioe);
				return;
			} catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!fKilled && fThread != null) {
					DebugPlugin.log(e);
				}
				return;
			}
            
            currentTime = System.currentTimeMillis();
            if (currentTime - lastSleep > 1000) {
                lastSleep = currentTime;
                try {
                    Thread.sleep(1); // just give up CPU to maintain UI responsiveness.
                } catch (InterruptedException e) {
                } 
            }
		}
		try {
			fStream.close();
		} catch (IOException e) {
			DebugPlugin.log(e);
		}
	}
	
	protected void kill() {
		fKilled= true;
	}

	public synchronized void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}
	protected void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					read();
				}
			}, "Output Stream Monitor");
            fThread.setDaemon(true);
            fThread.setPriority(Thread.MIN_PRIORITY);
			fThread.start();
		}
	}
	
	public synchronized void setBuffered(boolean buffer) {
		fBuffered = buffer;
	}
	public synchronized void flushContents() {
		fContents.setLength(0);
	}
	public synchronized boolean isBuffered() {
		return fBuffered;
	}
	private ContentNotifier getNotifier() {
		return new ContentNotifier();
	}
	
	class ContentNotifier implements ISafeRunnable {
		
		private IStreamListener fListener;
		private String fText;
		
		public void handleException(Throwable exception) {
			DebugPlugin.log(exception);
		}

		public void run() throws Exception {
			fListener.streamAppended(fText, POutputStreamMonitor.this);
		}

		public void notifyAppend(String text) {
			if (text == null)
				return;
			fText = text;
			for (Iterator i=fListeners.iterator(); i.hasNext();) {
				fListener = (IStreamListener)i.next();
				Platform.run(this);
			}
			fListener = null;
			fText = null;		
		}
	}
}

