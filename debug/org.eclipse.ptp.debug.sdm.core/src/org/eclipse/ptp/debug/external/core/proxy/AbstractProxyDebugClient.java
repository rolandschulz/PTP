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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyConnectedEvent;
import org.eclipse.ptp.core.proxy.event.IProxyDisconnectedEvent;
import org.eclipse.ptp.core.proxy.event.IProxyErrorEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.core.proxy.event.IProxyOKEvent;
import org.eclipse.ptp.core.proxy.event.IProxyTimeoutEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugDataExpValueEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugOKEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugPartialAIFEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSetThreadSelectEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStackInfoDepthEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSuspendEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugTypeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugVarsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugEventFactory;

public abstract class AbstractProxyDebugClient extends AbstractProxyClient implements IProxyDebugClient,IProxyEventListener {
	private List<IProxyDebugEventListener>	listeners = Collections.synchronizedList(new ArrayList<IProxyDebugEventListener>());
	private boolean		waiting = false;
	private boolean		connected = false;
	private final long WAIT_CONNECTION = 60000;
	private final ReentrantLock waitLock = new ReentrantLock();
	private final Condition waitCondition = waitLock.newCondition();
	
	public AbstractProxyDebugClient() {
		super(new ProxyDebugEventFactory());
		super.addProxyEventListener(this);
	}
	
	public void closeConnection() throws IOException {
		super.removeProxyEventListener(this);
		sessionFinish();
		//sessionDestroy();
	}
	
	public void checkConnection() throws IOException {
		waitLock.lock();
		try {
			if (!connected) {
				waiting = true;
				try {
					waitCondition.await(WAIT_CONNECTION, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is cancelled
				}
			}
		} finally {
			waitLock.unlock();
		}
		if (!connected) {
			closeConnection();
			throw new IOException("Cannot connect to proxy server.");
		}
	}
	
	public boolean waitForConnect(IProgressMonitor monitor) throws IOException {
		System.out.println("debug: waiting for connect");
		waitLock.lock();
		try {
			while (!connected && !monitor.isCanceled()) {
				waiting = true;
				try {
					waitCondition.await(1000, TimeUnit.MILLISECONDS);
					System.out.println("still waiting...");
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is cancelled
				}
			}
			if (monitor.isCanceled()) {
				closeConnection();
				return false;
			}
		} finally {
			waitLock.unlock();
			monitor.done();
		}
		return true;
	}
	
	public void addProxyDebugEventListener(IProxyDebugEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeProxyDebugEventListener(IProxyDebugEventListener listener) {
		listeners.remove(listener);
	}
		
	public void handleProxyConnectedEvent(IProxyConnectedEvent e) {
		System.out.println("debug: received connected event");
		waitLock.lock();
		try {
			connected = true;
			if (waiting) {
				waitCondition.signal();
				waiting = false;
			}
		} finally {
			waitLock.unlock();
		}
	}

	public void handleProxyDisconnectedEvent(IProxyDisconnectedEvent e) {
		System.out.println("debug: received disconnected event");
	}
	
	public void handleProxyErrorEvent(IProxyErrorEvent e) {
		System.out.println("debug: received error event");
	}

	public void handleProxyOKEvent(IProxyOKEvent e) {
		System.out.println("debug: received ok event");
	}

	public void handleProxyTimeoutEvent(IProxyTimeoutEvent e) {
		System.out.println("debug: received timeout event");
	}
	
	public void handleProxyExtendedEvent(IProxyExtendedEvent e) {
		if (e instanceof IProxyDebugArgsEvent) {
			fireProxyDebugArgsEvent((IProxyDebugArgsEvent) e);
		} else if (e instanceof IProxyDebugBreakpointHitEvent) {
			fireProxyDebugBreakpointHitEvent((IProxyDebugBreakpointHitEvent) e);
		} else if (e instanceof IProxyDebugBreakpointSetEvent) {
			fireProxyDebugBreakpointSetEvent((IProxyDebugBreakpointSetEvent) e);
		} else if (e instanceof IProxyDebugDataEvent) {
			fireProxyDebugDataEvent((IProxyDebugDataEvent) e);
		} else if (e instanceof IProxyDebugDataExpValueEvent) {
			fireProxyDebugDataExpValueEvent((IProxyDebugDataExpValueEvent) e);
		} else if (e instanceof IProxyDebugExitEvent) {
			fireProxyDebugExitEvent((IProxyDebugExitEvent) e);
		} else if (e instanceof IProxyDebugErrorEvent) {
			fireProxyDebugErrorEvent((IProxyDebugErrorEvent) e);
		} else if (e instanceof IProxyDebugInfoThreadsEvent) {
			fireProxyDebugInfoThreadsEvent((IProxyDebugInfoThreadsEvent) e);
		} else if (e instanceof IProxyDebugInitEvent) {
			fireProxyDebugInitEvent((IProxyDebugInitEvent) e);
		} else if (e instanceof IProxyDebugMemoryInfoEvent) {
			fireProxyDebugMemoryInfoEvent((IProxyDebugMemoryInfoEvent) e);
		} else if (e instanceof IProxyDebugOKEvent) {
			fireProxyDebugOKEvent((IProxyDebugOKEvent) e);
		} else if (e instanceof IProxyDebugPartialAIFEvent) {
			fireProxyDebugPartialAIFEvent((IProxyDebugPartialAIFEvent) e);
		} else if (e instanceof IProxyDebugSetThreadSelectEvent) {
			fireProxyDebugSetThreadSelectEvent((IProxyDebugSetThreadSelectEvent) e);
		} else if (e instanceof IProxyDebugSignalEvent) {
			fireProxyDebugSignalEvent((IProxyDebugSignalEvent) e);
		} else if (e instanceof IProxyDebugSignalExitEvent) {
			fireProxyDebugSignalExitEvent((IProxyDebugSignalExitEvent) e);
		} else if (e instanceof IProxyDebugSignalsEvent) {
			fireProxyDebugSignalsEvent((IProxyDebugSignalsEvent) e);
		} else if (e instanceof IProxyDebugStackframeEvent) {
			fireProxyDebugStackframeEvent((IProxyDebugStackframeEvent) e);
		} else if (e instanceof IProxyDebugStackInfoDepthEvent) {
			fireProxyDebugStackInfoDepthEvent((IProxyDebugStackInfoDepthEvent) e);
		} else if (e instanceof IProxyDebugStepEvent) {
			fireProxyDebugStepEvent((IProxyDebugStepEvent) e);
		} else if (e instanceof IProxyDebugSuspendEvent) {
			fireProxyDebugSuspendEvent((IProxyDebugSuspendEvent) e);
		} else if (e instanceof IProxyDebugTypeEvent) {
			fireProxyDebugTypeEvent((IProxyDebugTypeEvent) e);
		} else if (e instanceof IProxyDebugVarsEvent) {
			fireProxyDebugVarsEvent((IProxyDebugVarsEvent) e);
		}
	}

	protected void fireProxyDebugArgsEvent(IProxyDebugArgsEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugArgsEvent(e);
		}
	}

	protected void fireProxyDebugBreakpointHitEvent(IProxyDebugBreakpointHitEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugBreakpointHitEvent(e);
		}
	}

	protected void fireProxyDebugBreakpointSetEvent(IProxyDebugBreakpointSetEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugBreakpointSetEvent(e);
		}
	}

	protected void fireProxyDebugDataEvent(IProxyDebugDataEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugDataEvent(e);
		}
	}

	protected void fireProxyDebugDataExpValueEvent(IProxyDebugDataExpValueEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugDataExpValueEvent(e);
		}
	}

	protected void fireProxyDebugExitEvent(IProxyDebugExitEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugExitEvent(e);
		}
	}

	protected void fireProxyDebugErrorEvent(IProxyDebugErrorEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugErrorEvent(e);
		}
	}

	protected void fireProxyDebugInfoThreadsEvent(IProxyDebugInfoThreadsEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugInfoThreadsEvent(e);
		}
	}

	protected void fireProxyDebugInitEvent(IProxyDebugInitEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugInitEvent(e);
		}
	}

	protected void fireProxyDebugMemoryInfoEvent(IProxyDebugMemoryInfoEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugMemoryInfoEvent(e);
		}
	}

	protected void fireProxyDebugOKEvent(IProxyDebugOKEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugOKEvent(e);
		}
	}
	protected void fireProxyDebugPartialAIFEvent(IProxyDebugPartialAIFEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugPartialAIFEvent(e);
		}
	}

	protected void fireProxyDebugSetThreadSelectEvent(IProxyDebugSetThreadSelectEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugSetThreadSelectEvent(e);
		}
	}

	protected void fireProxyDebugSignalEvent(IProxyDebugSignalEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugSignalEvent(e);
		}
	}

	protected void fireProxyDebugSignalExitEvent(IProxyDebugSignalExitEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugSignalExitEvent(e);
		}
	}

	protected void fireProxyDebugSignalsEvent(IProxyDebugSignalsEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugSignalsEvent(e);
		}
	}

	protected void fireProxyDebugStackframeEvent(IProxyDebugStackframeEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugStackframeEvent(e);
		}
	}

	protected void fireProxyDebugStackInfoDepthEvent(IProxyDebugStackInfoDepthEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugStackInfoDepthEvent(e);
		}
	}

	protected void fireProxyDebugStepEvent(IProxyDebugStepEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugStepEvent(e);
		}
	}

	protected void fireProxyDebugSuspendEvent(IProxyDebugSuspendEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugSuspendEvent(e);
		}
	}

	protected void fireProxyDebugTypeEvent(IProxyDebugTypeEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugTypeEvent(e);
		}
	}

	protected void fireProxyDebugVarsEvent(IProxyDebugVarsEvent e) {
		IProxyDebugEventListener[] la = listeners.toArray(new IProxyDebugEventListener[0]);
		for (IProxyDebugEventListener listener : la) {
			listener.handleProxyDebugVarsEvent(e);
		}
	}

}
