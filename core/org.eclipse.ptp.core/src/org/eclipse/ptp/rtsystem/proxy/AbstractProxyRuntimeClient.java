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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;
import org.eclipse.ptp.core.proxy.event.ProxyConnectedEvent;
import org.eclipse.ptp.core.proxy.event.ProxyDisconnectedEvent;
import org.eclipse.ptp.core.proxy.event.ProxyErrorEvent;
import org.eclipse.ptp.core.proxy.event.ProxyOKEvent;
import org.eclipse.ptp.core.proxy.event.ProxyTimeoutEvent;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeConnectedEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeDisconnectedEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeTimeoutEvent;

public abstract class AbstractProxyRuntimeClient extends AbstractProxyClient implements IProxyEventListener {
	private boolean logEvents = true;
	protected final String proxyPath;
	protected final boolean launchManually;
	protected BitSet waitEvents = new BitSet();
	protected Queue events = new Queue();
	protected List 	listeners = Collections.synchronizedList(new ArrayList());

	public AbstractProxyRuntimeClient(String proxyPath, boolean launchManually) {
		super();
		super.addEventListener(this);
		this.proxyPath = proxyPath;
		this.launchManually = launchManually;
	}
	
	/**
	 * Set flag to control the logging of events
	 * 
	 * @param logEvents - event logging is turned on if true, turned off otherwise 
	 */ 
	public void setEventLogging(boolean logEvents) {
		this.logEvents = logEvents;
	}

    public void sendCommand(String cmd) throws IOException {
        super.sendCommand(cmd);
    }
    
    public void initialize() throws IOException {
		if (logEvents) System.out.println("ProxyRuntimeClient: Waiting on initialize.");

		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
		sendCommand("INIT");
		waitForRuntimeEvent();
    }

    public void sendEvents() throws IOException {
		if (logEvents) System.out.println("ProxyRuntimeClient: Starting event stream.");
		sendCommand("SEND_EVENTS");  	
    }

    public void haltEvents() throws IOException {
		if (logEvents) System.out.println("ProxyRuntimeClient: Halting event stream.");
		sendCommand("HALT_EVENTS");  	
    }

    public void initiateDiscovery() throws IOException {
    	sendCommand("SEND_EVENTS");
    }

	public void run(String[] args) throws IOException {
		sendCommand("RUN", args);
	}
	
	public void terminateJob(int jobId) throws IOException {
		sendCommand("TERMJOB", Integer.toString(jobId));
	}
	
	public void addRuntimeEventListener(IProxyRuntimeEventListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public void handleEvent(IProxyEvent event) {
		IProxyRuntimeEvent e = null;
		if (listeners == null) {
			System.out.println("AbstractProxyRuntimeClient.handleEvent() no listeners!");
			return;
		}
		
		if (event instanceof ProxyOKEvent) {
			e = (IProxyRuntimeEvent) ProxyRuntimeEvent.toEvent(((ProxyOKEvent) event).getData());
		} else if (event instanceof ProxyErrorEvent) {
			e = new ProxyRuntimeErrorEvent(null, ((ProxyErrorEvent)event).getErrorCode(), ((ProxyErrorEvent)event).getErrorMessage());
		} else if (event instanceof ProxyConnectedEvent) {
			e = new ProxyRuntimeConnectedEvent();
		} else if (event instanceof ProxyDisconnectedEvent) {
			e = new ProxyRuntimeDisconnectedEvent( ((ProxyDisconnectedEvent)event).wasError());
		} else if (event instanceof ProxyTimeoutEvent) {
			e = new ProxyRuntimeTimeoutEvent();
		}
		
		if (e != null) {
			synchronized (listeners) {
				Iterator i = listeners.iterator();
				while (i.hasNext()) {
					IProxyRuntimeEventListener listener = (IProxyRuntimeEventListener) i.next();
					listener.handleEvent(e);
				}
			}
		}
	}
	
	public boolean startup(final IProgressMonitor monitor) {
		final String proxy_path = proxyPath;
		
		if (logEvents) {
			System.out.println("ProxyRuntimeClient - firing up proxy, waiting for connection.  Please wait!  This can take a minute . . .");
			System.out.println("PROXY_SERVER path = '" + proxyPath + "'");
		}

		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_CONNECTED);
			sessionCreate();
			if (launchManually) {
				if (monitor != null) {
					monitor.subTask("Waiting for manual lauch of proxy on port " + getSessionPort() + "...");
				}
			} else {
				Thread runThread = new Thread("Proxy Server Thread") {
					public void run() {
						String[] cmd = new String[2];
						cmd[0] = proxy_path;
						cmd[1] = "--port="+getSessionPort();
						if (logEvents)
							System.out.println("RUNNING PROXY SERVER COMMAND: '"+cmd[0]+" "+cmd[1]+"'");
						
						try {
							Process process = Runtime.getRuntime().exec(cmd);
							final BufferedReader err_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
							final BufferedReader out_reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

							new Thread(new Runnable() {
								public void run() {
									try {
										String output;
										while ((output = out_reader.readLine()) != null) {
											if (logEvents) System.out.println("++++++++++ ptp_lsf_proxy: " + output);
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}, "Program output Thread").start();
							
							new Thread(new Runnable() {
								public void run() {
									try {
										String line;
										while ((line = err_reader.readLine()) != null) {
											PTPCorePlugin.log(line);
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}, "Error output Thread").start();
						} catch(IOException e) {
							PTPCorePlugin.errorDialog("Running Proxy Server", null, e);
							if (monitor != null) {
								monitor.setCanceled(true);
							}
						}
					}
				};
				runThread.setDaemon(true);
				runThread.start();
			}
			
			if (logEvents) System.out.println("ProxyRuntimeClient: Waiting on accept.");
			waitForRuntimeEvent(monitor);

		} catch (IOException e) {
			System.err.println("Exception starting up proxy. :(");
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			return false;
		}
		return true;
	}

	public void shutdown() {
		try {
			if (logEvents) System.out.println("ProxyRuntimeClient: shutting down server...");
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sessionFinish();
			waitForRuntimeEvent();
			System.out.println("ProxyRuntimeClient: server shut down.");
		} catch (IOException e) {
			PTPCorePlugin.log(e);
		}
	}

	protected void setWaitEvent(int eventID) {
		waitEvents.set(eventID);
		waitEvents.set(IProxyRuntimeEvent.EVENT_RUNTIME_ERROR); // always check for errors
	}

	protected IProxyRuntimeEvent waitForRuntimeEvent() throws IOException {
		return waitForRuntimeEvent(null);
	}

	protected synchronized IProxyRuntimeEvent waitForRuntimeEvent(IProgressMonitor monitor) throws IOException {
		IProxyRuntimeEvent event = null;
		
		System.out.println("LSFProxyRuntimeClient waiting on " + waitEvents.toString());
		while (this.events.isEmpty()) {
    			try {
    				wait(500);
    			} catch (InterruptedException e) {
    				System.err.println("Interrupted exception.");
    			}
    			if (monitor != null && monitor.isCanceled()) {
    				throw new IOException("Cancelled by user");
    			}
		}
		System.out.println("LSFProxyRuntimeClient awoke!");
		try {
			event = (IProxyRuntimeEvent) this.events.removeItem();
		} catch (InterruptedException e) {
			waitEvents.clear();
			throw new IOException(e.getMessage());
		}
   		if (event instanceof ProxyRuntimeErrorEvent) {
   	   		waitEvents.clear();
   			throw new IOException(((ProxyRuntimeErrorEvent)event).getErrorMessage());
   		}
   		waitEvents.clear();
   		return event;
	}

	/*
	 * Only handle events we're interested in
	 */
    public synchronized void handleEvent(IProxyRuntimeEvent e) {
		if (logEvents) System.out.println("ProxyRuntimeClient got event: " + e.toString());
		
		if (waitEvents.get(e.getEventID())) {
			if (logEvents) System.out.println("ProxyRuntimeClient notifying...");
			this.events.addItem(e);
			notifyAll();
		}
		
    }

}
