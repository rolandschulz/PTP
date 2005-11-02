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

package org.eclipse.ptp.rtsystem.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.ProxyErrorEvent;
import org.eclipse.ptp.core.proxy.event.ProxyOKEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeConnectedEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeEvent;

public abstract class AbstractProxyRuntimeClient extends AbstractProxyClient {
	protected List		listeners = new ArrayList(2);
	private boolean		waiting = false;

	public AbstractProxyRuntimeClient(String host, int port) {
		super(host, port);
	}
	
	public synchronized void sessionCreate(boolean wait) throws IOException {
		super.sessionCreate();
		if (wait) {
			waiting = true;
			try {
				wait();
			} catch (InterruptedException e) {
			}
			waiting = false;
		}
	}
	
	public void addEventListener(IProxyRuntimeEventListener listener) {
		listeners.add(listener);
	}
			
	protected synchronized void fireEvent(IProxyEvent event) {
		IProxyRuntimeEvent e = null;
		System.out.println("got event " + event);
		if (listeners == null)
			return;
		
		switch (event.getEventID()) {
		case IProxyEvent.EVENT_OK:
			e = (IProxyRuntimeEvent) ProxyRuntimeEvent.toEvent(((ProxyOKEvent) event).getData());
			break;
			
		case IProxyEvent.EVENT_ERROR:
			e = new ProxyRuntimeErrorEvent(null, ((ProxyErrorEvent)event).getErrorCode(), ((ProxyErrorEvent)event).getErrorMessage());
			break;
			
		case IProxyEvent.EVENT_CONNECTED:
			e = new ProxyRuntimeConnectedEvent();
			if (waiting) {
				System.out.println("NOTIFYIN!");
				notify();
			}
			break;
		}
		
		if (e != null) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				IProxyRuntimeEventListener listener = (IProxyRuntimeEventListener) i.next();
				listener.fireEvent(e);
			}
		}
	}
}
