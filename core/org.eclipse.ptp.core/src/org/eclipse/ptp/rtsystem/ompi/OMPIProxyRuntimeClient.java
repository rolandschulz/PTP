package org.eclipse.ptp.rtsystem.ompi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.ControlSystemChoices;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.MonitoringSystemChoices;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.internal.core.CoreUtils;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
	
	public boolean startup() {
		System.out.println("OMPIProxyRuntimeClient - firing up proxy, waiting for connecting.  Please wait!  This can take a minute . . .");
		
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		String proxyPath = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
		System.out.println("ORTE_SERVER path = '"+proxyPath+"'");
		if(proxyPath.equals("")) {
			String err = "Could not start the ORTE server.  Check the "+
				"PTP/Open RTE preferences page and be certain that the path and arguments "+
				"are correct.  Defaulting to Simulation Mode.";
			System.err.println(err);
			CoreUtils.showErrorDialog("ORTE Server Start Failure", err, null);

			int MSI = MonitoringSystemChoices.SIMULATED;
			int CSI = ControlSystemChoices.SIMULATED;
							
			preferences.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSI);
			preferences.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSI);

			PTPCorePlugin.getDefault().savePluginPreferences();
			
			return false;
		}

		final String proxyPath2 = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_CONNECTED);
			sessionCreate();
			
			Thread runThread = new Thread("Proxy Server Thread") {
				public void run() {
					String cmd;
					
					Runtime rt = Runtime.getRuntime ();
					
					cmd = proxyPath2 + " --port="+getSessionPort();
					
					System.out.println("RUNNING PROXY SERVER COMMAND: '"+cmd+"'");
					
					try {
						Process process = rt.exec(cmd);
						InputStreamReader reader = new InputStreamReader (process.getErrorStream());
						BufferedReader buf_reader = new BufferedReader (reader);
						
						String line = buf_reader.readLine();
						if (line != null) {
							CoreUtils.showErrorDialog("Running Proxy Server", line, null);
						}
						/*
						String line;
						while ((line = buf_reader.readLine ()) != null) {
							System.out.println ("ORTE PROXY SERVER: "+line);
						}
						*/
					} catch(IOException e) {
						String err;
						err = "Error running proxy server with command: '"+cmd+"'.";
						e.printStackTrace();
						System.out.println(err);
						CoreUtils.showErrorDialog("Running Proxy Server", err, null);
					}
				}
			};
			runThread.start();
			
			System.out.println("Waiting on accept.");
			waitForRuntimeEvent();
		} catch (IOException e) {
			System.err.println("Exception starting up proxy. :(");
			System.exit(1);
		}
		
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sendCommand("STARTDAEMON");
			waitForRuntimeEvent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}	

	private void setWaitEvent(int eventID) {
		waitEvents.set(eventID);
		waitEvents.set(IProxyRuntimeEvent.EVENT_RUNTIME_ERROR); // always check for errors
	}
	
	private synchronized IProxyRuntimeEvent waitForRuntimeEvent() throws IOException {
    		IProxyRuntimeEvent event = null;
    		
    		try {
        		System.out.println("OMPIProxyRuntimeClient waiting on " + waitEvents.toString());
        		while (this.events.isEmpty())
        			wait();
        		System.out.println("OMPIProxyRuntimeClient awoke!");
        		event = (IProxyRuntimeEvent) this.events.removeItem();
        		if (event instanceof ProxyRuntimeErrorEvent) {
        	   		waitEvents.clear();
        			throw new IOException(((ProxyRuntimeErrorEvent)event).getErrorMessage());
        		}
    		} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
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
