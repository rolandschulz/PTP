package org.eclipse.ptp.rtsystem.ompi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.BitSet;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.ControlSystemChoices;
import org.eclipse.ptp.core.IModelManager;
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
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodeAttributeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodesEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessAttributeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessesEvent;

public class OMPIProxyRuntimeClient extends ProxyRuntimeClient implements IRuntimeProxy, IProxyRuntimeEventListener {
	protected Queue events = new Queue();
	protected BitSet waitEvents = new BitSet();
	protected IModelManager modelManager;
	
	public OMPIProxyRuntimeClient(IModelManager modelManager) {
		super();
		super.addRuntimeEventListener(this);
		this.modelManager = modelManager;
	}
	
	public int runJob(String[] args) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NEWJOB);
		run(args);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNewJobEvent)event).getJobID();
	}
	
	public int getJobProcesses(int jobID) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_PROCS);
		getProcesses(jobID);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeProcessesEvent)event).getNumProcs();
	}
	
	public String[] getProcessAttributesBlocking(int jobID, int procID, String keys) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_PROCATTR);
		getProcessAttribute(jobID, procID, keys);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeProcessAttributeEvent)event).getValues();
	}
	
	public String[] getAllProcessesAttribuesBlocking(int jobID, String keys) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_PROCATTR);
		getProcessAttribute(jobID, -1, keys);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeProcessAttributeEvent)event).getValues();
	}
	
	public int getNumNodesBlocking(int machineID) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NODES);
		getNodes(machineID);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNodesEvent)event).getNumNodes();
	}
	
	public String[] getNodeAttributesBlocking(int machID, int nodeID, String keys) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NODEATTR);
		getNodeAttribute(machID, nodeID, keys);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNodeAttributeEvent)event).getValues();
	}
	
	public String[] getAllNodesAttributesBlocking(int machID, String keys) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NODEATTR);
		getNodeAttribute(machID, -1, keys);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNodeAttributeEvent)event).getValues();
	}
	
	public boolean startup(final IProgressMonitor monitor) {
		System.out.println("OMPIProxyRuntimeClient - firing up proxy, waiting for connecting.  Please wait!  This can take a minute . . .");
		
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		String proxyPath = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
		System.out.println("ORTE_SERVER path = '"+proxyPath+"'");
		/* if the orte_server path is unset, let's try and find it for them */
		if(proxyPath.equals("")) {
			/* if they don't have the orte_server path set, let's try and give them a default that might help */
			boolean found_orte_server = false;
			
			URL url = Platform.find(Platform.getBundle(PTPCorePlugin.PLUGIN_ID), new Path("/"));
			String ipath2, ipath3;
			ipath2 = new String("");
			ipath3 = new String("");

			if (url != null) {
				try {
					File path = new File(Platform.asLocalURL(url).getPath());
					String ipath = path.getAbsolutePath();
					System.out.println("Plugin install dir = '"+ipath+"'");
					
					/* org.eclipse.ptp.orte.linux.x86_64_1.0.0
					   org.eclipse.ptp.orte.$(OS).$(ARCH)_$(VERSION) */
					String ptp_version = (String)PTPCorePlugin.getDefault().getBundle().getHeaders().get("Bundle-Version");
					System.out.println("PTP Version = "+ptp_version);
					Properties p = System.getProperties();
					String os = p.getProperty("osgi.os");
					String arch = p.getProperty("osgi.arch");
					System.out.println("osgi.os = "+os);
					System.out.println("osgi.arch = "+arch);
					/* OK, we might have it . . . */
					if(os != null && arch != null && ptp_version != null) {
						String combo = "org.eclipse.ptp.core."+os+"."+arch+"_"+ptp_version;
						System.out.println("Searching for directory: "+combo);
						int idx = ipath.indexOf(combo);
						/* if we found it */
						if(idx > 0) {
							ipath2 = ipath.substring(0, idx) + "org.eclipse.ptp.orte."+os+"."+arch+"_"+ptp_version+"/bin/orte_server";
							System.out.println("Searching for "+ipath2);
							File f = new File(ipath2);
							if(f.exists()) {
								preferences.setValue(PreferenceConstants.ORTE_SERVER_PATH, ipath2);
								found_orte_server = true;
							}
						}
						else ipath2 = combo;
					}
					
					if(!found_orte_server) {
						int idx = ipath.indexOf("org.eclipse.ptp.core");
						ipath3 = ipath.substring(0, idx) + "org.eclipse.ptp.orte/orte_server";
						System.out.println("Searching for "+ipath3);
						File f = new File(ipath3);
						if(f.exists()) {
							preferences.setValue(PreferenceConstants.ORTE_SERVER_PATH, ipath3);
							found_orte_server = true;
						}
					}
				} catch(Exception e) { 	}
			}
			
			/* OK we checked everywhere we knew, send them to the Simulator instead */
			if(!found_orte_server) {
				String err = "Could not start the ORTE server.  Check the "+
					"PTP/Open RTE preferences page and be certain that the path and arguments "+
					"are correct.  Checked for 'orte_server' in locations '"+ipath2+"' and '"+
					ipath3+"'.  Defaulting to Simulation Mode.";
				System.err.println(err);
				PTPCorePlugin.errorDialog("ORTE Server Start Failure", err, null);

				int MSI = MonitoringSystemChoices.SIMULATED;
				int CSI = ControlSystemChoices.SIMULATED;
								
				preferences.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSI);
				preferences.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSI);

				PTPCorePlugin.getDefault().savePluginPreferences();
				
				return false;
			}
		}

		final String proxyPath2 = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_CONNECTED);
			sessionCreate();
			if (preferences.getBoolean(PreferenceConstants.ORTE_LAUNCH_MANUALLY)) {
				monitor.subTask("Waiting for manual lauch of orte_server on port "+getSessionPort()+"...");
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
											System.out.println("++++++++++ orte_server: " + output);
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
			// TODO Auto-generated catch block
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
