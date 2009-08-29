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

package org.eclipse.ptp.proxy.debug.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.ptp.internal.proxy.debug.event.ProxyDebugEventFactory;
import org.eclipse.ptp.proxy.client.AbstractProxyClient;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEventFactory;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEventListener;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugExitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugInitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugOKEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSetThreadSelectEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalExitEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalsEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStackInfoDepthEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStackframeEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStepEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSuspendEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugTypeEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugVarsEvent;
import org.eclipse.ptp.proxy.event.IProxyConnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyDisconnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyErrorEvent;
import org.eclipse.ptp.proxy.event.IProxyEventListener;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent;
import org.eclipse.ptp.proxy.event.IProxyOKEvent;
import org.eclipse.ptp.proxy.event.IProxyTimeoutEvent;

public abstract class AbstractProxyDebugClient extends AbstractProxyClient implements IProxyDebugClient,IProxyEventListener {
	
	private List<IProxyDebugEventListener>	listeners = 
		Collections.synchronizedList(new ArrayList<IProxyDebugEventListener>());

	protected boolean					waiting = false;
	protected boolean					timeout = false;
	protected final ReentrantLock		waitLock = new ReentrantLock();
	protected final Condition			waitCondition = waitLock.newCondition();
	protected volatile DebugProxyState	state;
	protected IProxyDebugEventFactory	factory;
	
	protected enum DebugProxyState {
		DISCONNECTED,
		DISCONNECTING,
		CONNECTED,
		CONNECTING
	}
	
	public AbstractProxyDebugClient() {
		super();
		this.factory = new ProxyDebugEventFactory();
		super.setEventFactory(factory);
	}
	
	/**
	 * Initialize the debugger connection.
	 * 
	 * @throws IOException
	 */
	public void doInitialize(int port) throws IOException {
		state = DebugProxyState.DISCONNECTED;
		addProxyEventListener(this);
		sessionCreate(port, 0);
		state = DebugProxyState.CONNECTING;
	}
	

	/**
	 * Shutdown the debugger connection. This can be called if we are either in the CONNECTED
	 * state, in which case we will receive a disconnected event, or in the CONNECTING state, 
	 * in which case we will receive an error event.
	 * 
	 * @throws IOException
	 */
	public void doShutdown() throws IOException {
		waitLock.lock();
		try {
			if (state == DebugProxyState.CONNECTING || state == DebugProxyState.CONNECTED) {
				state = DebugProxyState.DISCONNECTING;
				sessionFinish();
				while (state == DebugProxyState.DISCONNECTING) {
					waiting = true;
					try {
						waitCondition.await(30000, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
					}
				}
			}
		} finally {
			waitLock.unlock();
			state = DebugProxyState.DISCONNECTED;
		}
		removeProxyEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.debug.client.IProxyDebugClient#addProxyDebugEventListener(org.eclipse.ptp.proxy.debug.client.event.IProxyDebugEventListener)
	 */
	public void addProxyDebugEventListener(IProxyDebugEventListener listener) {
		listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.debug.client.IProxyDebugClient#removeProxyDebugEventListener(org.eclipse.ptp.proxy.debug.client.event.IProxyDebugEventListener)
	 */
	public void removeProxyDebugEventListener(IProxyDebugEventListener listener) {
		listeners.remove(listener);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.core.proxy.event.IProxyConnectedEvent)
	 */
	public void handleEvent(IProxyConnectedEvent e) {
		waitLock.lock();
		try {
			state = DebugProxyState.CONNECTED;
			if (waiting) {
				waitCondition.signalAll();
				waiting = false;
			}
		} finally {
			waitLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.core.proxy.event.IProxyDisconnectedEvent)
	 */
	public void handleEvent(IProxyDisconnectedEvent e) {
		waitLock.lock();
		try {
			if (state == DebugProxyState.DISCONNECTING) {
				state = DebugProxyState.DISCONNECTED;
				if (waiting) {
					waitCondition.signalAll();
					waiting = false;
				}
			}
		} finally {
			waitLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.core.proxy.event.IProxyErrorEvent)
	 */
	public void handleEvent(IProxyErrorEvent e) {
		// Do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.core.proxy.event.IProxyMessageEvent)
	 */
	public void handleEvent(IProxyMessageEvent e) {
		waitLock.lock();
		try {
			if (state == DebugProxyState.DISCONNECTING) {
				state = DebugProxyState.DISCONNECTED;
				if (waiting) {
					waitCondition.signalAll();
					waiting = false;
				}
			}
		} finally {
			waitLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.core.proxy.event.IProxyOKEvent)
	 */
	public void handleEvent(IProxyOKEvent e) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.core.proxy.event.IProxyTimeoutEvent)
	 */
	public void handleEvent(IProxyTimeoutEvent e) {
		waitLock.lock();
		try {
			timeout = true;
			if (waiting) {
				waitCondition.signalAll();
				waiting = false;
			}
		} finally {
			waitLock.unlock();
		}
	}
	
	public void handleEvent(IProxyExtendedEvent e) {
		if (e instanceof IProxyDebugArgsEvent) {
			fireProxyDebugArgsEvent((IProxyDebugArgsEvent) e);
		} else if (e instanceof IProxyDebugBreakpointHitEvent) {
			fireProxyDebugBreakpointHitEvent((IProxyDebugBreakpointHitEvent) e);
		} else if (e instanceof IProxyDebugBreakpointSetEvent) {
			fireProxyDebugBreakpointSetEvent((IProxyDebugBreakpointSetEvent) e);
		} else if (e instanceof IProxyDebugDataEvent) {
			fireProxyDebugDataEvent((IProxyDebugDataEvent) e);
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
