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

package org.eclipse.ptp.debug.external.core.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.ProxyErrorEvent;
import org.eclipse.ptp.core.proxy.event.ProxyOKEvent;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugEvent;

public abstract class AbstractProxyDebugClient extends AbstractProxyClient implements IProxyEventListener {
	protected List		listeners = new ArrayList(2);
	private boolean		waiting = false;
	private boolean		connected = false;

	public AbstractProxyDebugClient() {
		super();
		super.addEventListener(this);
	}
	
	public synchronized void waitForConnect() throws IOException {
		try {
			while (!connected) {
				waiting = true;
				wait();
			}
		} catch (InterruptedException e) {
		}
	}
	
	protected void sendCommand(String cmd, BitList set) throws IOException {
		String setStr = encodeBitSet(set);
		this.sendCommand(cmd, setStr);
	}
	
	protected void sendCommand(String cmd, BitList set, String arg1) throws IOException {
		String setStr = encodeBitSet(set);
		this.sendCommand(cmd, setStr, arg1);
	}


	protected void sendCommand(String cmd, BitList set, String arg1, String arg2) throws IOException {
		String setStr = encodeBitSet(set);
		this.sendCommand(cmd, setStr, arg1, arg2);
	}

	protected void sendCommand(String cmd, BitList set, String[] args) throws IOException {
		String setStr = encodeBitSet(set);
		this.sendCommand(cmd, setStr, args);
	}
	
	public void addEventListener(IProxyDebugEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeEventListener(IProxyDebugEventListener listener) {
		listeners.remove(listener);
	}
		
	public synchronized void handleEvent(IProxyEvent event) {
		IProxyDebugEvent e = null;
//PDebugUtils.println("AbstractProxyDebugClientgot event " + event);
		if (listeners == null)
			return;
		
		switch (event.getEventID()) {
		case IProxyEvent.EVENT_OK:
			e = (IProxyDebugEvent) ProxyDebugEvent.toEvent(((ProxyOKEvent) event).getData());
			break;
			
		case IProxyEvent.EVENT_ERROR:
			e = new ProxyDebugErrorEvent(new BitList(0), ((ProxyErrorEvent)event).getErrorCode(), ((ProxyErrorEvent)event).getErrorMessage());
			break;
			
		case IProxyEvent.EVENT_CONNECTED:
			connected = true;
			if (waiting) {
				notifyAll();
				waiting = false;
			}
			return;
		}
		
		if (e != null) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				IProxyDebugEventListener listener = (IProxyDebugEventListener) i.next();
				listener.handleEvent(e);
			}
		}
	}
}
