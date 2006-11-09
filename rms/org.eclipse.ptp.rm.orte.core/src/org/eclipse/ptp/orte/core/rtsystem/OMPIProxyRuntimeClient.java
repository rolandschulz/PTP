package org.eclipse.ptp.orte.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;

public class OMPIProxyRuntimeClient extends ProxyRuntimeClient implements IRuntimeProxy, IProxyRuntimeEventListener {
	protected Queue events = new Queue();
	protected BitSet waitEvents = new BitSet();
	private final String proxyPath;
	private final boolean launchManually;
	
	public OMPIProxyRuntimeClient(String proxyPath, boolean launchManually) {
		super();
		this.proxyPath = proxyPath;
		this.launchManually = launchManually;
		super.addRuntimeEventListener(this);
	}
	
	public void runJob(String[] args) throws IOException {
		run(args);
	}
	
	public boolean startup(final IProgressMonitor monitor) {
		System.out.println("OMPIProxyRuntimeClient - firing up proxy, waiting for connecting.  Please wait!  This can take a minute . . .");
		
		System.out.println("ORTE_SERVER path = '"+proxyPath+"'");
		
		final String proxyPath2 = proxyPath;
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_CONNECTED);
			sessionCreate();
			if (launchManually) {
				monitor.subTask("Waiting for manual lauch of ptp_orte_proxy on port "+getSessionPort()+"...");
			} else {
				Thread runThread = new Thread("Proxy Server Thread") {
					public void run() {
						String[] cmd = new String[2];
						cmd[0] = proxyPath2;
						cmd[1] = "--port="+getSessionPort();
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
											System.out.println("++++++++++ ptp_orte_proxy: " + output);
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
			
			System.out.println("Waiting on accept.");
			waitForRuntimeEvent(monitor);

			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sendCommand("STARTDAEMON");
			waitForRuntimeEvent();
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
			System.out.println("OMPIProxyRuntimeClient shutting down server...");
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sessionFinish();
			waitForRuntimeEvent();
			System.out.println("OMPIProxyRuntimeClient shut down.");
		} catch (IOException e) {
			PTPCorePlugin.log(e);
		}
	}	

	private void setWaitEvent(int eventID) {
		waitEvents.set(eventID);
		waitEvents.set(IProxyRuntimeEvent.EVENT_RUNTIME_ERROR); // always check for errors
	}

	private IProxyRuntimeEvent waitForRuntimeEvent() throws IOException {
		return waitForRuntimeEvent(null);
	}

	private synchronized IProxyRuntimeEvent waitForRuntimeEvent(IProgressMonitor monitor) throws IOException {
		IProxyRuntimeEvent event = null;
		
		System.out.println("OMPIProxyRuntimeClient waiting on " + waitEvents.toString());
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
		System.out.println("OMPIProxyRuntimeClient awoke!");
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
		System.out.println("OMPIProxyRuntimeClient got event: " + e.toString());
		
		if (waitEvents.get(e.getEventID())) {
			System.out.println("OMPIProxyRuntimeClient notifying...");
			this.events.addItem(e);
			notifyAll();
		}
		
    }

}
