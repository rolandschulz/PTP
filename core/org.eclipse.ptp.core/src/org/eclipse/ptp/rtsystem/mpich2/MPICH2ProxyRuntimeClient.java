package org.eclipse.ptp.rtsystem.mpich2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.ControlSystemChoices;
import org.eclipse.ptp.core.MonitoringSystemChoices;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNewJobEvent;

public class MPICH2ProxyRuntimeClient extends ProxyRuntimeClient implements IRuntimeProxy, IProxyRuntimeEventListener {
	protected Queue events = new Queue();
	protected BitSet waitEvents = new BitSet();
	
	public MPICH2ProxyRuntimeClient() {
		super();
		super.addRuntimeEventListener(this);
	}
	
	public int runJob(String[] args) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NEWJOB);
		run(args);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNewJobEvent)event).getJobID();
	}
	
	public boolean startup(final IProgressMonitor monitor) {
		System.out.println("MPICH2ProxyRuntimeClient - firing up proxy, waiting for connecting.  Please wait!  This can take a minute . . .");
		
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		String proxyPath = preferences.getString(PreferenceConstants.MPICH2_PROXY_PATH);
		System.out.println("MPICH2_SERVER path = '"+proxyPath+"'");
		
		/* if they don't have the ptp_orte_proxy path set, let's try and give them a default that might help */
		if(proxyPath.equals("")) {
			proxyPath = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp.mpich2.proxy", "ptp_mpich2_proxy.py");
			if(proxyPath != null) preferences.setValue(PreferenceConstants.MPICH2_PROXY_PATH, proxyPath);
			else {
				String err = "Could not find the MPICH2 server ('ptp_mpich2_proxy').  "+
					"File not found in the fragment directory or developer directories.  "+
					"Defaulting to Simulation Mode.";
				System.err.println(err);
				//PTPCorePlugin.errorDialog("ORTE Server Start Failure", err, null);

				int MSI = MonitoringSystemChoices.SIMULATED;
				int CSI = ControlSystemChoices.SIMULATED;
							
				preferences.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSI);
				preferences.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSI);

				PTPCorePlugin.getDefault().savePluginPreferences();
			
				return false;
			}
        }

		final String proxyPath2 = preferences.getString(PreferenceConstants.MPICH2_PROXY_PATH);
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_CONNECTED);
			sessionCreate();
			if (preferences.getBoolean(PreferenceConstants.MPICH2_LAUNCH_MANUALLY)) {
				monitor.subTask("Waiting for manual lauch of ptp_mpich2_proxy on port "+getSessionPort()+"...");
			} else {
				Thread runThread = new Thread("Proxy Server Thread") {
					public void run() {
						String cmd = proxyPath2 + " --port="+getSessionPort();
						System.out.println("RUNNING PROXY SERVER COMMAND: '"+cmd+"'");
						
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
			System.out.println("MPICH2ProxyRuntimeClient shutting down server...");
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sessionFinish();
			waitForRuntimeEvent();
			System.out.println("MPICH2ProxyRuntimeClient shut down.");
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
		
		System.out.println("MPICH2ProxyRuntimeClient waiting on " + waitEvents.toString());
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
		System.out.println("MPICH2ProxyRuntimeClient awoke!");
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
		System.out.println("MPICH2ProxyRuntimeClient got event: " + e.toString());
		
		if (waitEvents.get(e.getEventID())) {
			System.out.println("MPICH2ProxyRuntimeClient notifying...");
			this.events.addItem(e);
			notifyAll();
		}
		
    }

}
